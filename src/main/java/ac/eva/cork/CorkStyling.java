package ac.eva.cork;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class CorkStyling {
    public static final TextColor PRIMARY_COLOR = TextColor.color(0xf98e40);
    public static final TextColor SECONDARY_COLOR = TextColor.color(0xf2ceb5);
    public static final Component PREFIX = Component.empty()
            .append(Component.text("[cork]", PRIMARY_COLOR))
            .appendSpace()
            .color(SECONDARY_COLOR);
    public static final Component INFO_LINE_PREFIX = Component.text("> ", PRIMARY_COLOR);
}
