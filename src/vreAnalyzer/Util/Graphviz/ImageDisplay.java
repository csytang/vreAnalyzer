package vreAnalyzer.Util.Graphviz;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageDisplay {
	public ImageDisplay(File imageFile,JPanel imagePanel) throws IOException{
		// 生成image实例根据文件
		BufferedImage image = ImageIO.read(imageFile);
		ImageIcon icon = new ImageIcon(image);
		
		// 生成Panel
		JLabel imglabel = new JLabel();
		imglabel.setIcon(icon);
		
		imagePanel = new JPanel(new BorderLayout());
		imagePanel.add(imglabel);
	}
}
