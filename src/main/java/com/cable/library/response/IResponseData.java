package com.cable.library.response;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Optional;

/**
 * Represents a response from a player to a {@link IUserInputRequest}.
 *
 * This allows for the plugin to ask the player for particular information and
 * respond to the information supplied back in a reusable and maintainable way.
 *
 * @param <T> the type of data response is sending back
 *
 * @author landonjw
 */
public interface IResponseData<T> {

	/**
	 * Gets the request that this response is intended for.
	 *
	 * @return the request this response is intended for
	 */
	IUserInputRequest<T> getRequestSource();

	/**
	 * Gets the player responding to the request.
	 *
	 * @return player responding
	 */
	EntityPlayerMP getPlayer();

	/**
	 * Checks if the response is valid.
	 *
	 * An example where this may return false is if a request wants an integer,
	 * and the player supplies a string that cannot be parsed into one.
	 *
	 * @return if the response is valid
	 */
	boolean validates();

	/**
	 * Gets the response value player sent back, if present.
	 *
	 * This may return an empty optional if the response could not be properly validated.
	 *
	 * @return response value player sent back, if present
	 */
	Optional<T> getResponse();

	/**
	 * Gets the exact message player responded back with.
	 *
	 * @return message player responded back with
	 */
	String getRawResponse();

}
