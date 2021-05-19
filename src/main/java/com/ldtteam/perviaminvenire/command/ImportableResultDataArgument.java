package com.ldtteam.perviaminvenire.command;

import com.google.gson.JsonObject;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsStorageManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ImportableResultDataArgument implements ArgumentType<String>
{
    private static final Collection<String> EXAMPLES = Arrays.asList(UUID.randomUUID().toString(),
      UUID.randomUUID().toString(),
      UUID.randomUUID().toString(),
      UUID.randomUUID().toString(),
      UUID.randomUUID().toString()
    );

    private static final ImportableResultDataArgument INSTANCE = new ImportableResultDataArgument();

    public static ImportableResultDataArgument getInstance()
    {
        return INSTANCE;
    }

    private ImportableResultDataArgument()
    {
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException
    {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
      final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        ICalculationResultsStorageManager.getInstance().getAvailableIdentifiers()
          .forEach(builder::suggest);

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    public static class Serializer implements IArgumentSerializer<ImportableResultDataArgument>
    {
        private static final Serializer INSTANCE = new Serializer();

        public static Serializer getInstance()
        {
            return INSTANCE;
        }

        private Serializer()
        {
        }

        @Override
        public void write(@NotNull final ImportableResultDataArgument argument, @NotNull final PacketBuffer buffer)
        {
        }

        @NotNull
        @Override
        public ImportableResultDataArgument read(@NotNull final PacketBuffer buffer)
        {
            return ImportableResultDataArgument.getInstance();
        }

        @Override
        public void write(@NotNull final ImportableResultDataArgument argument, @NotNull final JsonObject jsonObject)
        {
        }
    }

}
