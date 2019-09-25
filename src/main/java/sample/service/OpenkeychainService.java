package sample.service;

import java.io.IOException;

import io.aergo.openkeychain.model.Challenge;
import io.aergo.openkeychain.model.Entry;
import io.aergo.openkeychain.model.Metadata;
import io.aergo.openkeychain.model.Response;

public interface OpenkeychainService {

	public Challenge createChallenge();
	
	public String recordRegistration(Response response, Metadata data) throws IOException;
	
	public String revokeRegistration(Response response) throws IOException;
	
	public boolean checkRegistration(Response response) throws IOException;
	
	public Entry fetchRegistration(Response response) throws IOException;
	
}
