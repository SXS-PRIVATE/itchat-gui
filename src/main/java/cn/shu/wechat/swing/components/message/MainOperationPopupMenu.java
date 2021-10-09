package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCMainOperationMenuItemUI;
import cn.shu.wechat.swing.frames.CreateGroupDialog;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.frames.SystemConfigDialog;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by 舒新胜 on 2017/6/5.
 */
public class MainOperationPopupMenu extends JPopupMenu {
    public MainOperationPopupMenu() {
        initMenuItem();
    }

    private void initMenuItem() {
        JMenuItem item1 = new JMenuItem("创建群聊");
        JMenuItem item2 = new JMenuItem("设置");

        item1.setUI(new RCMainOperationMenuItemUI());
        item1.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateGroupDialog();
            }
        });
        ImageIcon icon1 = IconUtil.getIcon(this,"/image/chat.png");
        icon1.setImage(icon1.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        item1.setIcon(icon1);
        item1.setIconTextGap(5);


        item2.setUI(new RCMainOperationMenuItemUI());
        item2.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println("系统设置");
                SystemConfigDialog dialog = new SystemConfigDialog(MainFrame.getContext(), true);
                dialog.setVisible(true);
            }
        });
        ImageIcon icon2 = IconUtil.getIcon(this,"/image/setting.png");
        icon2.setImage(icon2.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        item2.setIcon(icon2);
        item2.setIconTextGap(5);


        this.add(item1);
        this.add(item2);

        setBorder(new LineBorder(Colors.SCROLL_BAR_TRACK_LIGHT));
        setBackground(Colors.FONT_WHITE);
    }

    /**
     * 弹出创建群聊窗口
     */
    private void showCreateGroupDialog() {
        CreateGroupDialog dialog = new CreateGroupDialog(null, true);
        dialog.setVisible(true);

        /*ShadowBorderDialog shadowBorderDialog = new ShadowBorderDialog(MainFrame.getContext(), true, dialog);
        shadowBorderDialog.setVisible(true);*/
    }
}