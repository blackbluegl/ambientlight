package org.ambientlight.debug;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BufferedImageDisplayOutput extends JFrame {

	private static final long serialVersionUID = 1L;
	BufferedImage imageContent;
	ImageBackgroundPanel imageBackground;
	
	public BufferedImageDisplayOutput(int sizeX, int sizeY, String title) {
		setTitle(title);
		setSize(sizeX, sizeY);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		imageBackground = new ImageBackgroundPanel(imageContent);
		getContentPane().add(imageBackground);
		imageBackground.setLayout(null);
		this.setVisible(true);
	}

	class ImageBackgroundPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		
		BufferedImage image;

		ImageBackgroundPanel(BufferedImage image) {
			this.image = image;
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 0, 0, this);
		}
	}

	public BufferedImage getImageContent() {
		return imageContent;
	}

	public void setImageContent(BufferedImage stripeContent) {
		this.imageContent = stripeContent;
		this.imageBackground.image = stripeContent;
		this.imageBackground.repaint();
	}
}
