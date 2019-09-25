package sample.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hera.api.model.AccountState;
import hera.api.model.Aer;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import hera.api.model.TxHash;
import hera.client.AergoClientBuilder;
import hera.key.AergoKey;
import hera.util.IoUtils;
import io.aergo.openkeychain.backend.AergoAdaptor;
import io.aergo.openkeychain.backend.AergoBackend;
import io.aergo.openkeychain.backend.Backend;
import io.aergo.openkeychain.core.KeyManager;
import io.aergo.openkeychain.core.Signer;
import io.aergo.openkeychain.core.SimpleKeyManager;
import io.aergo.openkeychain.provider.ContextProvider;
import io.aergo.openkeychain.util.KeyUtils;
import io.aergo.openkeychain.util.PropertiesUtils;
import sample.App;
import sample.util.AwaitUtils;

public class AergoBackendProvider implements BackendProvider {
	
	static final Logger logger = LoggerFactory.getLogger(AergoBackendProvider.class);
	
	
	public static final String CONTRACT_PATH = "/contracts/openkeychain.bin";
	public static final String CONFIG_PATH = "config.xml";
	public static final String SCRATCH_PATH = "scratch.xml";
	
	public static final String AERGO_ENDPOINT = "aergo.endpoint";
	public static final String CHARGER_ENCRYPTED = "charger.encrypted";
	public static final String CHARGER_PASSWORD = "charger.password";
	
	public static final String OWNER_ENCRYPTED = "owner.encrypted";
	public static final String OWNER_PASSWORD = "owner.password";
	public static final String OWNER_ADDRESS = "owner.address";
	public static final String CONTRACT_ADDRESS = "contract.address";
	
	
	Backend backend;
	Properties config;
	Properties scratch;
	
	KeyManager keyManager;
	
	public AergoBackendProvider() {
		try {
			// config and scratch properties
			final File scratchFile = initScratchFile(SCRATCH_PATH);
			logger.info("scratch.abspath: {}", scratchFile.getAbsolutePath());
			logger.info("scratch.exists: {}", scratchFile.exists());
			this.scratch = PropertiesUtils.init(scratchFile);
			this.config = PropertiesUtils.load(App.class, CONFIG_PATH);
			
			
			// backend
			final String endpoint = config.getProperty(AERGO_ENDPOINT);
			logger.info("aergo.endpoint: {}", endpoint);
			this.backend = AergoBackend.builder()
					.aergoClient(new AergoClientBuilder()
							.withEndpoint(endpoint)
							.withNonBlockingConnect()
							.withRetry(5, 300, TimeUnit.MILLISECONDS)
							.build())
					.build();
			
			// key
			String ownerEncrypted = this.scratch.getProperty(OWNER_ENCRYPTED, null);
			String ownerPassword = this.scratch.getProperty(OWNER_PASSWORD, null);
			if (ownerEncrypted == null) {
				// create new key
				logger.info("create new key");
				ownerPassword = ContextProvider.defaultProvider.getContext();
				final AergoKey ownerKey = KeyUtils.createAergoKey();
				final String ownerAddress = ownerKey.getAddress().getEncoded();
				ownerEncrypted = ownerKey.export(ownerPassword).getEncoded();
				this.scratch.put(OWNER_ENCRYPTED, ownerEncrypted);
				this.scratch.put(OWNER_PASSWORD, ownerPassword);
				this.scratch.put(OWNER_ADDRESS, ownerAddress);
				PropertiesUtils.store(this.scratch, scratchFile);
			}
			this.keyManager = new SimpleKeyManager(AergoKey.of(ownerEncrypted, ownerPassword));
			logger.info("owner.address: {}", keyManager.fetchAddress());
			this.backend.getAdaptor(AergoAdaptor.class).bindNonce(this.keyManager.fetchAddress());
			fund(this.keyManager.fetchAddress(), Aer.of("10", Aer.Unit.AERGO));
			
			// contract address
			String contractAddress = scratch.getProperty(CONTRACT_ADDRESS, null);
			if (contractAddress == null) {
				logger.info("deplot contract");
				contractAddress = deployContract();
				this.scratch.put(CONTRACT_ADDRESS, contractAddress);
				PropertiesUtils.store(this.scratch, scratchFile);
			}
			logger.info("contract.address: {}", contractAddress);
			this.backend.setContract(contractAddress);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public File initScratchFile(final String path) throws IOException {
		File fp = new File(path);
		if (!fp.exists()) {
			final File parentFile = fp.getParentFile();
			if (parentFile != null && !parentFile.exists()) {
				parentFile.mkdirs();
			}
			fp.createNewFile();
		}
		return fp;
	}
	
	public void fund(String address, Aer amount) {
		final AergoAdaptor adaptor = this.backend.getAdaptor(AergoAdaptor.class);
		final KeyManager charger = new SimpleKeyManager(AergoKey.of(
				config.getProperty(CHARGER_ENCRYPTED),
				config.getProperty(CHARGER_PASSWORD)));
		final AccountState state = adaptor.getState(charger.fetchAddress());
		adaptor.getNonceProvider().bindNonce(state);
		if (state.getBalance().compareTo(amount) >= 0) {
			return;
		}
		final TxHash txHash = adaptor.sendcoin(charger.getSigner(), address, amount);
		AwaitUtils.txConfirmed(adaptor, txHash);
	}
	
	public String deployContract() throws IOException {
		final String encodedContract = new String(IoUtils.from(
				getClass().getResourceAsStream(CONTRACT_PATH)), StandardCharsets.UTF_8).trim();
		final AergoAdaptor adaptor = this.backend.getAdaptor(AergoAdaptor.class);
		final ContractTxHash txHash = adaptor.deployContract(this.keyManager.getSigner(), encodedContract);
		AwaitUtils.txConfirmed(adaptor, txHash);
		final ContractTxReceipt receipt = adaptor.getContractReceipt(txHash);
		return receipt.getContractAddress().getEncoded();
	}
	
	
	@Override
	public void close() throws IOException {
		this.backend.close();
	}

	@Override
	public Backend getBackend() {
		return backend;
	}

	@Override
	public Properties getConfig() {
		return config;
	}

	@Override
	public Properties getScratch() {
		return scratch;
	}

	@Override
	public Signer getSigner() {
		return keyManager.getSigner();
	}

}
