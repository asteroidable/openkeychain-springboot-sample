package sample.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import io.aergo.openkeychain.backend.Backend;
import io.aergo.openkeychain.model.Challenge;
import io.aergo.openkeychain.model.Entry;
import io.aergo.openkeychain.model.Metadata;
import io.aergo.openkeychain.model.Response;
import io.aergo.openkeychain.server.RegistrationManager;
import lombok.Getter;
import lombok.Setter;

@Service
public class OpenkeychainServiceImpl implements OpenkeychainService {

	@Getter @Setter
	RegistrationManager manager = null;
	
	
	public OpenkeychainServiceImpl() {
		try {
			final Backend backend = BackendProvider.defaultProvider.getBackend();
			this.manager = RegistrationManager.builder()
					.backend(backend)
					.publishers(backend.getPublishers())
					.signer(BackendProvider.defaultProvider.getSigner())
					.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String[] getPublishers() {
		return getManager().getPublishers().getPublishers().toArray(new String[] {});
	}

	@Override
	public Challenge createChallenge() {
		return getManager().createChallenge();
	}

	@Override
	public String recordRegistration(Response response, Metadata data) throws IOException {
		if (!getManager().checkResponse(response)) {
			return null;
		}
		return getManager().recordRegistration(Entry.of(response.getCertificate(), data));
	}

	@Override
	public String revokeRegistration(Response response) throws IOException {
		if (!getManager().checkResponse(response)) {
			return null;
		}
		return getManager().revokeRegistration(response.getCertificate());
	}

	@Override
	public boolean checkRegistration(String address) throws IOException {
		return getManager().checkRegistration(address);
	}

	@Override
	public Entry fetchRegistration(String address) throws IOException {
		return getManager().fetchRegistration(address);
	}
	
}
