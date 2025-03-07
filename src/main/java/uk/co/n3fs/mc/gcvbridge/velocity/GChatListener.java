package uk.co.n3fs.mc.gcvbridge.velocity;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.lucko.gchat.api.events.GChatMessageFormedEvent;
import net.kyori.adventure.text.Component;
import uk.co.n3fs.mc.gcvbridge.GCVBridge;
import uk.co.n3fs.mc.gcvbridge.util.TextUtil;

public class GChatListener {

    private final GCVBridge plugin;
    private final String webhook;
    private Boolean has_webhook = false;
    private WebhookClient client = null;

    public GChatListener(GCVBridge plugin) {
        this.plugin = plugin;

        this.webhook = plugin.getConfig().getOutWebhook();

        if (this.webhook != null && !this.webhook.isEmpty()) {
            this.has_webhook = true;

            WebhookClientBuilder builder = new WebhookClientBuilder(this.webhook);

            // builder.setThreadFactory((job) -> {
            //     Thread thread = new Thread(job);
            //     thread.setName("Hello");
            //     thread.setDaemon(true);
            //     return thread;
            // });

            //builder.setWait(true);
            this.client = builder.build();
        }
    }

    @Subscribe
    public void onGChatMessage(GChatMessageFormedEvent event) {

        Player player = event.getSender();

        if (plugin.getConfig().isRequireSendPerm() && !player.hasPermission("gcvb.send")) return;

        this.sendToDiscord(player, event.getMessage(), event.getRawMessage());
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onLogin(LoginEvent e) {
		if (e.getPlayer().hasPermission("gcvb.silentjoin")) return;
		
        Player player = e.getPlayer();
        this.sendToDiscord(player, "```fix\nlogged on\n```", null);
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onLogout(DisconnectEvent e) {
		if (e.getPlayer().hasPermission("gcvb.silentquit")) return;
		
        Player player = e.getPlayer();
        this.sendToDiscord(player, "```fix\nlogged off\n```", null);
    }

    /**
     * Broadcast a message to Discord
     *
     * @param source        The originating player
     * @param message       The message as a Component
     * @param raw_message   The raw message as a string
     */
    public void sendToDiscord(Player source, Component message, String raw_message) {
        final String msg = TextUtil.stripString(TextUtil.toMarkdown(message));
        this.sendToDiscord(source, msg, raw_message);
    }

    /**
     * Broadcast a message to Discord
     *
     * @param source        The originating player
     * @param message       The message as a Component
     * @param raw_message   The raw message as a string
     */
    public void sendToDiscord(Player source, String message, String raw_message) {

        if (raw_message == null) {
            raw_message = message;
        }

        if (this.has_webhook) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();

            //GChatPlayer gplayer = new GChatPlayer(source);

            String name = source.getUsername();

            builder.setUsername(name);

            String avatar_url = "https://api.mineatar.io/head/" + source.getUniqueId() + "?overlay";

            builder.setAvatarUrl(avatar_url);

            builder.setContent(raw_message);

            this.client.send(builder.build());
        }
		else {
        plugin.getConfig().getOutChannels(plugin.getDApi())
                .forEach(textChannel -> textChannel.sendMessage(message));
		}

    }

}
