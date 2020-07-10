package com.cable.library.response;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Allows for messages to be sent to players with the intention of them responding back.
 *
 * A specified number of choices may be defined to limit what the player may
 * send back via the builder with {@link IUserInputRequest.Builder#choices(Map)},
 * or this can be null to create an all-encompassing reply.
 *
 * If no choices are defined, you should utilize {@link IResponseData#getRawResponse()}}
 * rather than {@link IResponseData#getResponse()}, as it will always return an empty optional.
 *
 * This is intended to be a flexible design to specify what data type the user should be
 * responding back with, and allowing for specifying what occurs on response.
 *
 * This utilizes {@link IResponseData} heavily, which is used to represent the data
 * a user responds with.
 *
 * @param <T> the type of data that the response should return
 *
 * @author landonjw
 */
public interface IUserInputRequest<T> {

	/**
	 * Gets the player request will be sent to
	 *
	 * @return the player request will be sent to
	 */
	EntityPlayerMP getPlayer();

	/**
	 * Gets the message to be sent to player upon processing, if present
	 *
	 * This will be an empty optional if no message is sent.
	 *
	 * @return the message to be sent to player, if present
	 */
	Optional<ITextComponent> getRequestMessage();

	/**
	 * Gets the message sent to player when their response is not a proper choice, if present
	 *
	 * This will be an empty optional if no message is sent.
	 *
	 * @return the message to be sent to player, if present
	 */
	Optional<ITextComponent> getInvalidResponseMessage();

	/**
	 * Sends the player the invalid response message, if there is one.
	 *
	 * If there is no invalid response message, this will do nothing.
	 */
	void sendInvalidResponseMessage();

	/**
	 * Gets all choices available to the user, and the values mapped to each choice, if present
	 *
	 * This will be an empty optional if there are no specified choices, and player may send any message.
	 *
	 * If no choices are defined, you should utilize {@link IResponseData#getRawResponse()}}
	 * rather than {@link IResponseData#getResponse()}, as it will always return an empty optional.
	 *
	 * @return the choices available to the user, if present
	 */
	Optional<Map<String, T>> getChoices();

	/**
	 * Cancels the request.
	 */
	void cancel();

	/**
	 * Creates a new instance of a builder for creating a {@link IUserInputRequest}.
	 *
	 * @param <T> type of data that response should return
	 * @return new instance of builder
	 */
	static <T> Builder<T> builder(){
		return new UserInputRequest.Builder<>();
	}

	/**
	 * Builder for creating a {@link IUserInputRequest}.
	 *
	 * In order for {@link #send(EntityPlayerMP)} to be successful, {@link #onResponse(Consumer)}
	 * must be invoked with a non-null value.
	 *
	 * @param <T> type of data that response should return
	 */
	interface Builder<T> {

		/**
		 * Sets the message to be sent to player upon processing
		 *
		 * @param message the message to be sent to player upon processing,
		 *                null to send no message
		 * @return builder with request message set
		 */
		Builder<T> requestMessage(@Nonnull ITextComponent message);

		/**
		 * Sets the message sent to player when their response is not a proper choice
		 *
		 * @param message the message to be sent to player on invalid response,
		 *                null to send no message
		 * @return builder with request messsage set
		 */
		Builder<T> invalidResponseMessage(@Nullable ITextComponent message);

		/**
		 * Sets the choices a player may respond with, and the values corresponding to each choice.
		 *
		 * An example would be setting choices for each Pokemon species, mapping to their corresponding EnumSpecies.
		 *
		 * If no choices are defined, you should utilize {@link IResponseData#getRawResponse()}}
		 * rather than {@link IResponseData#getResponse()}, as it will always return an empty optional.
		 *
		 * @param choices the choices player may respond with and values corresponding to each choice
		 * @return builder with choices set
		 */
		Builder<T> choices(@Nullable Map<String, T> choices);

		/**
		 * Defines the behaviour that should be invoked when a player responds to the request.
		 *
		 * The {@link IResponseData} may be used to decide what should occur upon
		 * player's choice.
		 *
		 * The predicate should return true if the response is completed, and false if
		 * the request should continue.
		 *
		 * @param responseBehaviour the behaviour to be invoked upon player response
		 * @return builder with behaviour set
		 */
		Builder<T> onResponse(@Nonnull Consumer<IResponseData<T>> responseBehaviour);

		/**
		 * Creates a new instance of {@link IUserInputRequest} from data in the builder and sends the request
		 * to the specified player.
		 *
		 * In order for {@link #send(EntityPlayerMP)} to be successful, {@link #onResponse(Consumer)}
		 * must be invoked with a non-null value.
		 *
		 * @param player the player to send request to
		 * @return new request instance from data in builder
		 */
		IUserInputRequest<T> send(@Nonnull EntityPlayerMP player);

	}

}