package com.ldtteam.perviaminvenire.command;

import com.google.gson.JsonObject;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsStorageManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
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

    public static class TypeInfo implements ArgumentTypeInfo<ImportableResultDataArgument, TypeInfo.Template>
    {
        private static final TypeInfo INSTANCE = new TypeInfo();

        public static TypeInfo getInstance()
        {
            return INSTANCE;
        }

        private TypeInfo()
        {
        }

        @Override
        public void serializeToNetwork(@NotNull Template template, @NotNull FriendlyByteBuf buffer) {
        }

        @Override
        public @NotNull Template deserializeFromNetwork(@NotNull FriendlyByteBuf buffer) {
            return new Template();
        }

        @Override
        public void serializeToJson(@NotNull Template template, @NotNull JsonObject json) {
        }

        @Override
        public @NotNull Template unpack(@NotNull ImportableResultDataArgument argument) {
            return new Template();
        }

        public static class Template implements ArgumentTypeInfo.Template<ImportableResultDataArgument> {

            @Override
            public @NotNull ImportableResultDataArgument instantiate(@NotNull CommandBuildContext context) {
                return ImportableResultDataArgument.getInstance();
            }

            @Override
            public @NotNull ArgumentTypeInfo<ImportableResultDataArgument, ?> type() {
                return TypeInfo.getInstance();
            }
        }
    }

}
