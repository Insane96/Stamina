package insane96mcp.stamina.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import insane96mcp.stamina.feature.StaminaHandler;
import insane96mcp.stamina.network.StaminaSync;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("insanestamina").requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(context -> {
                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "players")) {
                                                StaminaHandler.setStamina(player, FloatArgumentType.getFloat(context, "amount"));
                                                StaminaSync.sync(player);
                                            }
                                            return 1;
                                        }))))
                .then(Commands.literal("regen")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(context -> {
                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "players")) {
                                                StaminaHandler.regenStamina(player, FloatArgumentType.getFloat(context, "amount"));
                                                StaminaSync.sync(player);
                                            }
                                            return 1;
                                        }))))
                .then(Commands.literal("consume")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(context -> {
                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "players")) {
                                                StaminaHandler.consumeStamina(player, FloatArgumentType.getFloat(context, "amount"));
                                                StaminaSync.sync(player);
                                            }
                                            return 1;
                                        }))))
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    float stamina = StaminaHandler.getStamina(player);
                                    context.getSource().sendSuccess(()
                                            -> Component.translatable("commands.insanestamina.get", player.getName(), stamina), false);
                                    return (int) stamina;
                                })))
        );
    }
}