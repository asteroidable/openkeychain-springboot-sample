package sample;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc
public class AppTest {
	
	@Autowired
	private MockMvc mvc;

	@Test
    public void testMain() throws Exception {
		mvc.perform(get("/"))
	        .andExpect(status().isOk())
	        .andExpect(content().string("openkeychain"));
    }

}
