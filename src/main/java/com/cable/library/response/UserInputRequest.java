package com.cable.library.response;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class UserInputRequest<T> implements IUserInputRequest<T> {

	private EntityPlayerMP player;
	private ITextComponent requestMessage, invalidResponseMessage;
	private Map<String, T> choices;
	private Consumer<IResponseData<T>> responseBehaviour;

	private UserInputRequest(@Nonnull UserInputRequest.Builder<T> builder, @Nonnull EntityPlayerMP player){
		this.requestMessage = builder.requestMessage;
		this.invalidResponseMessage = builder.invalidResponseMessage;
		this.choices = builder.choices;
		this.responseBehaviour = Objects.requireNonNull(builder.responseBehaviour, "response behaviour must not be null");
		this.player = Objects.requireNonNull(player, "player must not be null");
		process();
	}

	@Override
	public EntityPlayerMP getPlayer() {
		return player;
	}

	@Override
	public Optional<ITextComponent> getRequestMessage() {
		return Optional.ofNullable(requestMessage);
	}

	@Override
	public Optional<ITextComponent> getInvalidResponseMessage() {
		return Optional.ofNullable(invalidResponseMessage);
	}

	@Override
	public void sendInvalidResponseMessage() {
		if(invalidResponseMessage != null){
			player.sendMessage(invalidResponseMessage);
		}
	}

	@Override
	public Optional<Map<String, T>> getChoices() {
		return Optional.ofNullable(choices);
	}

	private void onResponse(IResponseData<T> response) {
		responseBehaviour.accept(response);
	}

	private void process() {
		if(requestMessage != null){
			player.sendMessage(requestMessage);
		}
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void cancel() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onMessage(ServerChatEvent event){
		if(event.getPlayer() == player){
			event.setCanceled(true);
			IResponseData<T> response = new ResponseData<>(this, event.getPlayer(), event.getMessage(), choices);
			onResponse(response);
		}
	}

	public static class Builder<T> implements IUserInputRequest.Builder<T> {

		private ITextComponent requestMessage, invalidResponseMessage;
		private Map<String, T> choices;
		private Consumer<IResponseData<T>> responseBehaviour;

		@Override
		public Builder<T> requestMessage(@Nullable ITextComponent message) {
			this.requestMessage = message;
			return this;
		}

		@Override
		public Builder<T> invalidResponseMessage(@Nullable ITextComponent message) {
			this.invalidResponseMessage = message;
			return this;
		}

		@Override
		public Builder<T> choices(@Nullable Map<String, T> choices) {
			this.choices = choices;
			return this;
		}

		@Override
		public IUserInputRequest.Builder<T> onResponse(@Nonnull Consumer<IResponseData<T>> responseBehaviour) {
			this.responseBehaviour = responseBehaviour;
			return this;
		}

		@Override
		public IUserInputRequest<T> send(@Nonnull EntityPlayerMP player) {
			Objects.requireNonNull(player, "player must not be null");
			Objects.requireNonNull(responseBehaviour, "response behaviour must not be null");
			return new UserInputRequest<>(this, player);
		}

	}

}
