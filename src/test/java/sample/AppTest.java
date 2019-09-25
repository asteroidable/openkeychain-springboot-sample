package sample;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;

import io.aergo.openkeychain.client.LoginManager;
import io.aergo.openkeychain.core.KeyManager;
import io.aergo.openkeychain.core.SimpleKeyManager;
import io.aergo.openkeychain.model.Challenge;
import io.aergo.openkeychain.model.Entry;
import io.aergo.openkeychain.model.Response;
import io.aergo.openkeychain.util.Jsonizer;
import lombok.Cleanup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc
public class AppTest {
	
	static final Logger logger = LoggerFactory.getLogger(AppTest.class);
	
	
	@Autowired
	private MockMvc mvc;
	
	
	@Test
    public void testMain() throws Exception {
		mvc.perform(get("/"))
	        .andExpect(status().isOk())
	        .andExpect(content().string("openkeychain"));
    }
	
	
	KeyManager clientKeyManager = null;
	LoginManager loginManager = null;
	
	@Test
	public void tests() throws Exception {
		// get publishers
		String[] publishers = getPublishers();
		logger.info("publishers: {}", Arrays.toString(publishers));
		
		// build loginManager
		this.loginManager = LoginManager.builder()
				.publishers(publishers)
				.build();
		
		testRecordRegistration();
		testRevokeRegistration();
		
	}
	
	
	public void testRecordRegistration() throws Exception {
		logger.info("### record registration");
		// request challenge
		Challenge challenge = requestChallenge();
		logger.info("challenge: {}", challenge.marshal());
		
		// check challenge
		boolean challengeCheck = loginManager.checkChallenge(challenge);
		logger.info("challenge.check: {}", challengeCheck);
		assertTrue(challengeCheck);
		
		// create new key
		this.clientKeyManager = new SimpleKeyManager();
		
		// create response
		Response response = loginManager.createResponse(challenge, clientKeyManager.getSigner());
		logger.info("response: {}", response.marshal());
		
		// record registration
		String txHash = recordRegistration(response);
		logger.info("txHash: {}", txHash);
		assertTrue(txHash != null && !txHash.isEmpty());
		
		// check registration
		boolean check = checkRegistration(clientKeyManager.fetchAddress());
		logger.info("check: {}", check);
		assertTrue(check);
		
		// fetch registration
		Entry entry = fetchRegistration(clientKeyManager.fetchAddress());
		logger.info("entry: {}", entry.marshal());
		assertNotNull(entry);
	}


	public void testRevokeRegistration() throws Exception {
		logger.info("### revoke registration");
		
		// request challenge
		Challenge challenge = requestChallenge();
		logger.info("challenge: {}", challenge.marshal());
		
		// check challenge
		boolean challengeCheck = loginManager.checkChallenge(challenge);
		logger.info("challenge.check: {}", challengeCheck);
		assertTrue(challengeCheck);
		
		// create response
		this.clientKeyManager = new SimpleKeyManager();
		Response response = loginManager.createResponse(challenge, clientKeyManager.getSigner());
		logger.info("response: {}", response.marshal());
		
		// revoke registration
		String txHash = revokeRegistration(response);
		assertTrue(txHash != null && !txHash.isEmpty());
		logger.info("txHash: {}", txHash);
		
		// check registration
		boolean check = checkRegistration(clientKeyManager.fetchAddress());
		logger.info("check: {}", check);
		assertFalse(check);
		
		// fetch registration
		Entry entry = fetchRegistration(clientKeyManager.fetchAddress());
		logger.info("entry: {}", entry);
		assertNull(entry);
	}



	public String[] getPublishers() throws Exception {
		MvcResult result = mvc.perform(get("/publishers"))
			.andExpect(status().isOk())
			.andReturn();
		
		return parse(result, new TypeReference<String[]>() {});
	}

	public Challenge requestChallenge() throws Exception {
		MvcResult result = mvc.perform(get("/challenge"))
			.andExpect(status().isOk())
			.andReturn();
		
		return parse(result, Challenge.class);
	}

	public String recordRegistration(Response response) throws Exception {
		MvcResult result = mvc.perform(post("/registration")
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.content(response.marshal()))
				.andReturn();
		return result.getResponse().getContentAsString();
	}

	public String revokeRegistration(Response response) throws Exception {
		MvcResult result = mvc.perform(delete("/registration")
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.content(response.marshal()))
				.andReturn();
		return result.getResponse().getContentAsString();
	}

	public boolean checkRegistration(String address) throws Exception {
		MvcResult result = mvc.perform(get("/registration/check").param("address", address))
				.andReturn();
		return parse(result, Boolean.class);
	}
	
	public Entry fetchRegistration(String address) throws Exception {
		MvcResult result = mvc.perform(get("/registration").param("address", address))
				.andReturn();
		logger.debug("fetch: {}", result.getResponse().getContentAsString());
		return parse(result, Entry.class);
	}



	public static <T> T parse(MvcResult result, TypeReference<T> typeRef) throws IOException {
		byte[] bytes = result.getResponse().getContentAsByteArray();
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		@Cleanup InputStream in = new ByteArrayInputStream(bytes);
		return Jsonizer.getInstance().getMapper().reader()
				.forType(typeRef).readValue(in);
	}

	public static <T> T parse(MvcResult result, Class<T> clazz) throws IOException {
		byte[] bytes = result.getResponse().getContentAsByteArray();
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		@Cleanup InputStream in = new ByteArrayInputStream(bytes);
		return Jsonizer.getInstance().getMapper().reader()
				.forType(clazz).readValue(in);
	}

}
