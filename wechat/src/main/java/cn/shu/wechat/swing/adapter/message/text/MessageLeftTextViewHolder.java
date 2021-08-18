package cn.shu.wechat.swing.adapter.message.text;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */
public class MessageLeftTextViewHolder extends BaseMessageViewHolder {
    public SizeAutoAdjustTextArea sender ;
    //public JLabel avatar = new JLabel();
    //public JLabel size = new JLabel();
    //public RCLeftTextMessageBubble text = new RCLeftTextMessageBubble();

    public SizeAutoAdjustTextArea text;
    public RCLeftImageMessageBubble messageBubble = new RCLeftImageMessageBubble();

    private JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    private JPanel messageAvatarPanel = new JPanel();
    private MessagePopupMenu popupMenu = new MessagePopupMenu();
    private boolean isGroup = true;

    public MessageLeftTextViewHolder(boolean isGroup) {
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    private void initComponents() {
        int maxWidth = (int) (MainFrame.getContext().currentWindowWidth * 0.5);
        text = new SizeAutoAdjustTextArea(maxWidth);
        text.setParseUrl(true);
        sender = new SizeAutoAdjustTextArea(maxWidth);
        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        sender.setFont(FontUtil.getDefaultFont(12));
        sender.setForeground(Colors.FONT_GRAY);

        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        messageBubble.add(text);

        JPanel senderMessagePanel = new JPanel();
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
        if (isGroup) {
            senderMessagePanel.add(sender);
            //群消息会显示群成员名称 这时候往上移10
            sender.setBorder(new EmptyBorder(0,messageBubble.getSalientPointPixel(),100,0));
        }
        JPanel contentPanel = new JPanel();
        contentPanel.add(messageBubble);
        contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        senderMessagePanel.add(contentPanel);
        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1)
                .setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)

                .setInsets(0, 5, 0, 0));
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}