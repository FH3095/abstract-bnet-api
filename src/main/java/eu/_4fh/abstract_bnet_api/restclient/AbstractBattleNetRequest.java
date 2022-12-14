package eu._4fh.abstract_bnet_api.restclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dmfs.httpessentials.HttpMethod;
import org.dmfs.httpessentials.HttpStatus;
import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.client.HttpRequestEntity;
import org.dmfs.httpessentials.client.HttpResponse;
import org.dmfs.httpessentials.client.HttpResponseHandler;
import org.dmfs.httpessentials.entities.EmptyHttpRequestEntity;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.headers.EmptyHeaders;
import org.dmfs.httpessentials.headers.Headers;
import org.dmfs.httpessentials.responsehandlers.FailResponseHandler;
import org.dmfs.httpessentials.responsehandlers.StringResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public abstract class AbstractBattleNetRequest<T> implements HttpRequest<T> {
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	@Override
	public HttpMethod method() {
		return HttpMethod.GET;
	}

	@Override
	public Headers headers() {
		return EmptyHeaders.INSTANCE;
	}

	@Override
	public HttpRequestEntity requestEntity() {
		return EmptyHttpRequestEntity.INSTANCE;
	}

	/**
	 * This constructs the result from the json response from battle-net.
	 */
	protected abstract T convertJsonToObject(final JSONObject obj);

	/**
	 * This has to return the request type, which is combined of the namespace
	 * and the information, if a locale is required.
	 */
	protected abstract BattleNetRequestType getRequestType();

	@Override
	public HttpResponseHandler<T> responseHandler(HttpResponse response)
			throws IOException, ProtocolError, ProtocolException {
		if (!HttpStatus.OK.equals(response.status())) {
			log.info(() -> "Error making request " + String.valueOf(response.requestUri()) + ": "
					+ String.valueOf(response.status().statusCode()) + " "
					+ String.valueOf(response.status().reason()));
			return FailResponseHandler.getInstance();
		}

		final String responseString = new StringResponseHandler("UTF-8").handleResponse(response);
		log.log(Level.FINEST, "Queried {0} and got {1}",
				new Object[] { response.requestUri().toString(), responseString });
		try {
			JSONObject obj = new JSONObject(responseString);
			return new HttpResponseHandler<T>() {

				@Override
				public T handleResponse(HttpResponse response) throws IOException, ProtocolError, ProtocolException {
					return convertJsonToObject(obj);
				}
			};
		} catch (JSONException e) {
			throw new ProtocolException(String.format("Can't decode JSON response %s", responseString), e);
		}
	}
}
