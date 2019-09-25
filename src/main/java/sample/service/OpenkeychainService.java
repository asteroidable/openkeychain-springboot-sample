package sample.service;

import java.io.IOException;

import io.aergo.openkeychain.model.Challenge;
import io.aergo.openkeychain.model.Entry;
import io.aergo.openkeychain.model.Metadata;
import io.aergo.openkeychain.model.Response;

public interface OpenkeychainService {
	
	public String[] getPublishers();

	public Challenge createChallenge();
	
	public String recordRegistration(Response response, Metadata data) throws IOException;
	
	public String revokeRegistration(Response response) throws IOException;
	
	public boolean checkRegistration(String address) throws IOException;
	
	public Entry fetchRegistration(String address) throws IOException;
	
}
