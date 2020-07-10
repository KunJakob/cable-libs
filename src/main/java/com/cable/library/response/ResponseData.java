package com.cable.library.response;

import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

public class ResponseData<T> implements IResponseData<T> {

	private IUserInputRequest<T> request;
	private EntityPlayerMP player;
	private String message;
	private Map<String, T> choices;

	public ResponseData(@Nonnull IUserInputRequest<T> request,
	                    @Nonnull EntityPlayerMP player,
	                    @Nonnull String message,
	                    @Nonnull Map<String, T> choices){
		this.request = request;
		this.player = player;
		this.message = message;
		this.choices = choices;
	}

	@Override
	public IUserInputRequest<T> getRequestSource() {
		return request;
	}

	@Override
	public EntityPlayerMP getPlayer() {
		return player;
	}

	@Override
	public boolean validates() {
		return choices == null || choices.containsKey(message);
	}

	@Override
	public Optional<T> getResponse() {
		return (choices == null) ? Optional.empty() : Optional.ofNullable(choices.get(message));
	}

	@Override
	public String getRawResponse() {
		return message;
	}

}
