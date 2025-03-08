package uk.co.n3fs.mc.gcvbridge;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import me.lucko.gchat.api.ChatFormat;
import me.lucko.gchat.api.GChatApi;
import ninja.leaping.configurate.ConfigurationNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GCVBConfig {

    private final ConfigurationNode root;

    private final String token;
    private final List<Long> inChannels;
    private final List<Long> outChannels;
    private final boolean playerlistEnabled;
    private final String playerlistFormat;
    private final String playerlistSeparator;
    private final int playerlistCommandRemoveDelay;
    private final int playerlistResponseRemoveDelay;

    private final String joinFormat;
    private final String quitFormat;
    private final boolean requireSeePerm;
    private final boolean requireSendPerm;
    private final List<String> out_webhooks;

    private final ChatFormat gchatDefaultFormat;

    private final Map<Long, ChatFormat> gchatChannelFormats;
    private final String neutronAlertFormat;

    public GCVBConfig(GChatApi gcApi, ConfigurationNode root) throws Exception {
        this.root = root;

        // discord section
        token = root.getNode("discord", "token").getString();
        out_webhooks = root.getNode("discord", "out-webhooks").getList(TypeToken.of(String.class));
        inChannels = root.getNode("discord", "in-channels").getList(TypeToken.of(Long.class));
        outChannels = root.getNode("discord", "out-channels").getList(TypeToken.of(Long.class));

        playerlistEnabled = root.getNode("discord", "playerlist", "enabled").getBoolean(true);
        playerlistFormat = root.getNode("discord", "playerlist", "format").getString("**{count} players online:** ```\n{players}\n```");
        playerlistSeparator = root.getNode("discord", "playerlist", "separator").getString(", ");
        playerlistCommandRemoveDelay = root.getNode("discord", "playerlist", "command-remove-delay").getInt(0);
        playerlistResponseRemoveDelay = root.getNode("discord", "playerlist", "response-remove-delay").getInt(10);

        if (token == null || token.isEmpty()) {
            throw new InvalidConfigException("You need to set a bot token in config.yml!");
        }
        // velocity settings
        joinFormat = root.getNode("velocity", "join-format").getString("**{player} joined the game**");
        quitFormat = root.getNode("velocity", "quit-format").getString("**{player} left the game**");
        requireSeePerm = root.getNode("velocity", "require-see-permission").getBoolean(false);
        requireSendPerm = root.getNode("velocity", "require-send-permission").getBoolean(false);

        // gchat settings
        String gchatDefaultFormatName = root.getNode("gchat", "default-format").getString("default");
        gchatDefaultFormat = gcApi.getFormats().stream()
                .filter(format -> format.getId().equalsIgnoreCase(gchatDefaultFormatName))
                .findFirst()
                .orElseThrow(() -> new InvalidConfigException("The format specified by default-format does not exist in the gChat config!"));


        ConfigurationNode gchatChannelFormatNames = root.getNode("gchat", "channel-formats");
        Map<Long, ChatFormat> gchatChannelFormatMap = new HashMap<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : gchatChannelFormatNames.getChildrenMap().entrySet()) {
            Object channel = entry.getKey();
            String format = entry.getValue().getString();
            if (channel.toString().matches("^[0-9]+$") && inChannels.contains(Long.parseLong(channel.toString()))) {
                gchatChannelFormatMap.put(
                        Long.parseLong(channel.toString()),
                        gcApi.getFormats().stream()
                                .filter(f -> f.getId().equalsIgnoreCase(format))
                                .findFirst()
                                .orElseThrow(() -> new InvalidConfigException("A format specified in channel-formats does not exist in the gChat config!"))
                );
            }
        }

        gchatChannelFormats = ImmutableMap.copyOf(gchatChannelFormatMap);

        // neutron settings
        neutronAlertFormat = root.getNode("neutron", "alert-format").getString("**BROADCAST** {message}");
    }

    public class InvalidConfigException extends Exception {
        InvalidConfigException(String message) {
            super(message);
        }
    }

    public String getToken() {
        return token;
    }

    public List<String> getOutWebhooks() {
        return out_webhooks;
    }

    public List<TextChannel> getInChannels(DiscordApi dApi) {
        return inChannels.stream()
            .map(dApi::getTextChannelById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public List<TextChannel> getOutChannels(DiscordApi dApi) {
        return outChannels.stream()
            .map(dApi::getTextChannelById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public boolean isPlayerlistEnabled() {
        return playerlistEnabled;
    }

    public String getPlayerlistFormat() {
        return playerlistFormat;
    }

    public String getPlayerlistSeparator() {
        return playerlistSeparator;
    }

    public int getPlayerlistCommandRemoveDelay() {
        return playerlistCommandRemoveDelay;
    }

    public int getPlayerlistResponseRemoveDelay() {
        return playerlistResponseRemoveDelay;
    }

    public String getJoinFormat() {
        return joinFormat;
    }

    public String getQuitFormat() {
        return quitFormat;
    }

    public boolean isRequireSeePerm() {
        return requireSeePerm;
    }

    public boolean isRequireSendPerm() {
        return requireSendPerm;
    }

    public ChatFormat getDefaultFormat() {
        return gchatDefaultFormat;
    }

    public Map<Long, ChatFormat> getChannelFormats() {
        return gchatChannelFormats;
    }

    public String getNeutronAlertFormat() {
        return neutronAlertFormat;
    }
}
