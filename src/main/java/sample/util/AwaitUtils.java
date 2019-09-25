package sample.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;

import hera.api.model.TxHash;
import io.aergo.openkeychain.backend.AergoAdaptor;

public class AwaitUtils {

	public static void until(Callable<Boolean> conditionEvaluator) {
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.pollInterval(300, TimeUnit.MILLISECONDS)
				.until(conditionEvaluator);
	}

	public static void txConfirmed(final AergoAdaptor adaptor, final TxHash txHash) {
		until(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return adaptor.getTransaction(txHash).isConfirmed();
			}
		});
	}

	public static void txConfirmed(final AergoAdaptor adaptor, final String txHash) {
		txConfirmed(adaptor, TxHash.of(txHash));
	}

}
