package sample;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.aergo.openkeychain.backend.AergoAdaptor;
import io.aergo.openkeychain.model.Challenge;
import io.aergo.openkeychain.model.Entry;
import io.aergo.openkeychain.model.Response;
import sample.service.BackendProvider;
import sample.service.OpenkeychainService;
import sample.util.AwaitUtils;

@RestController
public class AppController {
	
	static final Logger logger = LoggerFactory.getLogger(AppController.class);
	
	
	@Autowired
	private OpenkeychainService openkeychain;
	
	
	@GetMapping("/")
	public String index() {
		return "openkeychain";
	}
	
	
	@GetMapping("/publishers")
	public String[] getPublishers() {
		logger.debug("getPublishers");
		return openkeychain.getPublishers();
	}
	
	@GetMapping("/challenge")
	public Challenge createChallenge() {
		logger.debug("createChallenge");
		return openkeychain.createChallenge();
	}
	
	
	protected final AergoAdaptor getAdaptor() {
		return BackendProvider.defaultProvider.getBackend().getAdaptor(AergoAdaptor.class);
	}
	
	@PostMapping("/registration")
	public String recordRegistration(@RequestBody Response response) throws IOException {
		logger.debug("recordRegistration: response={}", response.marshal());
		String txHash = openkeychain.recordRegistration(response, null);
		AwaitUtils.txConfirmed(getAdaptor(), txHash);
		return txHash;
	}
	
	@DeleteMapping("/registration")
	public String revokeRegistration(@RequestBody Response response) throws IOException {
		logger.debug("revokeRegistration: response={}", response.marshal());
		String txHash = openkeychain.revokeRegistration(response);
		AwaitUtils.txConfirmed(getAdaptor(), txHash);
		return txHash;
	}

	@GetMapping("/registration")
	public Entry fetchRegistration(@RequestParam("address") String address) throws IOException {
		logger.debug("fetchRegistration: address={}", address);
		if (address == null || address.isEmpty()) {
			return null;
		}
		return openkeychain.fetchRegistration(address);
	}
	
	@GetMapping("/registration/check")
	public boolean checkRegistration(@RequestParam("address") String address) throws IOException {
		logger.debug("checkRegistration: address={}", address);
		if (address == null || address.isEmpty()) {
			return false;
		}
		return openkeychain.checkRegistration(address);
	}

}
