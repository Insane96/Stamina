package insane96mcp.stamina.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.stamina.feature.StaminaHandler;
import insane96mcp.stamina.network.StaminaSync;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.BiConsumer;

public class SCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("insanestamina").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("set")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(context -> applyToPlayers(context, "set", StaminaHandler::setStamina)))))
                .then(Commands.literal("regen")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(context -> applyToPlayers(context, "regen", StaminaHandler::regenStamina)))))
                .then(Commands.literal("consume")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(context -> applyToPlayers(context, "consume", StaminaHandler::consumeStamina)))))
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    float stamina = StaminaHandler.getStamina(player);
                                    context.getSource().sendSuccess(()
                                            -> Component.translatable("commands.insanestamina.get", player.getDisplayName(), InsaneLib.ONE_DECIMAL_FORMATTER.format(stamina)), false);
                                    return (int) stamina;
                                })))
        );
    }

    /**
     * Applies the action to every targeted player, syncs them and sends the feedback. Returns the number of players affected
     */
    private static int applyToPlayers(CommandContext<CommandSourceStack> context, String action, BiConsumer<ServerPlayer, Float> staminaAction) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (ServerPlayer player : players) {
            staminaAction.accept(player, amount);
            StaminaSync.sync(player);
        }
        String formattedAmount = InsaneLib.ONE_DECIMAL_FORMATTER.format(amount);
        if (players.size() == 1)
            context.getSource().sendSuccess(() -> Component.translatable("commands.insanestamina.%s.success.single".formatted(action), players.iterator().next().getDisplayName(), formattedAmount), true);
        else
            context.getSource().sendSuccess(() -> Component.translatable("commands.insanestamina.%s.success.multiple".formatted(action), players.size(), formattedAmount), true);
        return players.size();
    }
}
