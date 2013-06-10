package reactor.spring.integration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import reactor.fn.Consumer;
import reactor.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link Consumer} implementation that sends every accepted object to a {@link MessageChannel}.
 *
 * @author Jon Brisbin
 */
public class MessageChannelConsumer<T> implements Consumer<T> {

	private final    MessageChannel           channel;
	private final    MessageChannel           errors;
	@SuppressWarnings("unchecked")
	private volatile Converter<T, Message<?>> converter;

	/**
	 * Create a new {@literal MessageChannelConsumer} using the given output channel.
	 *
	 * @param channel The {@link MessageChannel} on which to publish accepted objects.
	 */
	public MessageChannelConsumer(@Nonnull MessageChannel channel) {
		this(channel, null);
	}

	/**
	 * Create a new {@literal MessageChannelConsumer} using the given output channel and report any errors on the given
	 * error channel.
	 *
	 * @param channel The {@link MessageChannel} on which to publish accepted objects.
	 * @param errors  The {@link MessageChannel} on which to publish errors.
	 */
	public MessageChannelConsumer(@Nonnull MessageChannel channel, @Nullable MessageChannel errors) {
		Assert.notNull(channel, "MessageChannel cannot be null.");
		this.channel = channel;
		this.errors = errors;
	}

	/**
	 * Set the {@link Converter} to use to turn Reactor {@link reactor.fn.Event Events} into Spring Integration {@link
	 * Message Messages}.
	 *
	 * @param converter The converter to use.
	 */
	public void setConverter(Converter<T, Message<?>> converter) {
		Assert.notNull("Converter cannot be null.");
		this.converter = converter;
	}

	@Override
	public void accept(T t) {
		MessageChannel ch;
		if (null != errors && Throwable.class.isInstance(t)) {
			ch = errors;
		} else {
			ch = channel;
		}
		ch.send(createMessage(t));
	}

	private Message<?> createMessage(T t) {
		Message<?> msg;
		if (null != converter) {
			msg = converter.convert(t);
		} else {
			msg = new GenericMessage<Object>(t);
		}
		return msg;
	}

}