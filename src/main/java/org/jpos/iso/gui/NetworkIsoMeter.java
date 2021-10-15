/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpos.iso.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.Timer;

@SuppressWarnings("serial")
public final class NetworkIsoMeter extends JComponent implements Runnable {

	Timer ti;
	Graphics img;
	Image im, imb;
	boolean scroll = true;
	private Thread repaintThread;
	private boolean smsConnected = false;
	private boolean ussdConnected = false;
	Font fontBig, fontSmall, capFont;
	Color color = new Color(255, 255, 255);
	final static int width = 255;
	final static int height = 60;
	final static int MAX_VALUE = 1000;
	public final static int mass = height / 2;
	int continueScroll, yPoints[], xPoints[];
	private String positiveCounter = "";
	private String negativeCounter = "";
	String positiveText = "", negativeText = "";
	int refreshPanel = 50, lastPositive, lastNegative;
	private String caption = "", status = "SUSPENDED";
	private int activeSessions = 0;
	private boolean updated = false;

	public NetworkIsoMeter(String channelName) {
		super();
		fontBig = new Font("Helvetica", Font.ITALIC, mass * 3 / 5);
		fontSmall = new Font("Helvetica", Font.PLAIN, 10);
		capFont = new Font("Helvetica", Font.BOLD, 10);
		yPoints = new int[width];
		xPoints = new int[width];
		for (int i = 0; i < width; i++) {
			xPoints[i] = i;
			yPoints[i] = mass;
		}
		setUssdConnected(true, "ONLINE");
		setCaption(channelName);
		positiveCounter = "";
	}

	public String capitalize(String text) {
		if (text != null) {
			try {
				StringBuilder builder = new StringBuilder();
				for (String word : text.toLowerCase().split("\\s")) {
					builder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase())
							.append(" ");
				}
				return builder.toString();
			} catch (Exception ex) {
				return text;
			}
		}
		return text;
	}

	public synchronized void start() {
		if (repaintThread == null) {
			repaintThread = new Thread(this, "TrafficMonitor");
			repaintThread.setPriority(Thread.MIN_PRIORITY);
			repaintThread.start();
		}
	}

	public void setValue(int val) {
		int y = mass - ((val % 1000) * height / 2000);
		yPoints[width - 1] = y;
		continueScroll = width;
		scroll();
	}

	public void setScroll(boolean scroll) {
		this.scroll = scroll;
	}

	public void setRefresh(int refreshPanel) {
		if (refreshPanel > 0) {
			this.refreshPanel = refreshPanel;
		}
	}

	public void setUssdConnected(boolean connected, String status) {
		if (this.isUssdConnected() != connected || !status.equals(getStatus())) {
			if (!scroll) {
				continueScroll = connected ? width : 1;
			}
			setStatus(connected ? "ONLINE" : status);
			this.setUssdConnected(connected);
			repaint();
		}
	}

	public void setPositiveCounter(int s) {
		positiveCounter = s <= 0 ? "" : String.format("%03d", s);
		setActiveSessions(s);
	}

	public void setNegativeCounter(int s) {
		negativeCounter = (s <= 0 ? (!isSmsConnected() ? "UP" : "") : String.format("%03d", s));
	}

	public void setValue(int val, String textString) {
		setValue(val);

		if (val >= 0) {
			positiveText = (textString != null) ? textString : "";
			lastPositive = 0;
		} else {
			negativeText = (textString != null) ? textString : "";
			lastNegative = 0;
		}
	}

	@Override
	public void paint(Graphics g) {
		if (repaintThread == null) {
			start();
		}
		plot();
		g.drawImage(im, 0, 0, null);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	private void scroll() {
		System.arraycopy(yPoints, 1, yPoints, 0, width - 1);
		if (continueScroll > 0) {
			continueScroll--;
		}
		yPoints[width - 1] = mass;
	}

	public void plot() {
		if (im == null) {
			im = createImage(width, height);
			img = im.getGraphics();
			img.setColor(Color.black);
			img.fillRect(0, 0, width, height);
			img.clipRect(0, 0, width, height);
			plotGrid();

			/*
			 * save a copy of the image
			 */
			imb = createImage(width, height);
			Graphics imbCopy = imb.getGraphics();
			imbCopy.drawImage(im, 0, 0, this);
		}
		img.drawImage(imb, 0, 0, this);
		if (continueScroll > 0) {
			scroll();
		}

		plotText(positiveText, lastPositive++, 3, mass - 10);
		plotText(negativeText, lastNegative++, 3, height - 10);
		plotCounters(getPositiveCounter(), getNegativeCounter());
		img.setColor(isUssdConnected() ? Color.green : Color.red);
		img.drawPolyline(xPoints, yPoints, width);
		plotCaption();
		plotStatus();
	}

	private void plotGrid() {
		img.setColor(Color.blue);
		for (int i = 0; i < width; i++) {
			if (i % 20 == 0) {
				img.drawLine(i, 0, i, height);
			}
		}
		for (int i = -1000; i < 1000; i += 200) {
			int y = mass + (i * height / 2000);
			img.drawLine(0, y, width, y);
		}
	}

	private void plotText(String t, int l, int x, int y) {
		if (t != null && continueScroll > 0) {
			img.setColor(Color.lightGray);
			img.setFont(fontBig);
			img.drawString(t, x, y);
		}
	}

	public void plotCaption() {
		img.setFont(capFont);
		int capLen = Math.round(getCaption().length() * 6.4f);
		int x = (width - capLen) / 2;
		img.setColor(isUssdConnected() ? Color.green : Color.red);
		img.drawString(getCaption(), x, 8);
	}

	public void plotStatus() {
		img.setFont(capFont);
		int capLen = Math.round(getStatus().length() * 6.4f);
		int x = (width - capLen) / 2;
		img.setColor(isUssdConnected() ? Color.green : Color.red);
		img.drawString(getStatus(), x, height - 1);
	}

	private void plotCounters(String p, String n) {
		img.setColor(Color.lightGray);
		img.setFont(fontSmall);
		img.drawString(p, width - 25, 13);

		img.setColor(isSmsConnected() ? Color.green : Color.red);
		img.drawString(n, width - 25, height - 3);
	}

	@Override
	public void run() {
		while (true) {
			if (continueScroll > 0 || !positiveText.equals("") || !negativeText.equals("") || isUpdated()) {
				setUpdated(false);
				try {
					repaint();
				} catch (Exception ex) {
					ex = null;
				}
				scroll();
			}
			try {
				Thread.sleep(refreshPanel);
			} catch (InterruptedException e) {
				// OK to ignore
			}
		}
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status.toUpperCase();
	}

	/**
	 * @return the activeSessions
	 */
	public int getActiveSessions() {
		return activeSessions;
	}

	/**
	 * @param activeSessions the activeSessions to set
	 */
	public void setActiveSessions(int activeSessions) {
		this.activeSessions = activeSessions;
	}

	/**
	 * @return the smsConnected
	 */
	public boolean isSmsConnected() {
		return smsConnected;
	}

	/**
	 * @param smsConnected the smsConnected to set
	 */
	public void setSmsConnected(boolean smsConnected) {
		this.smsConnected = smsConnected;
		setUpdated(true);
	}

	/**
	 * @return the ussdConnected
	 */
	public boolean isUssdConnected() {
		return ussdConnected;
	}

	/**
	 * @param ussdConnected the ussdConnected to set
	 */
	public void setUssdConnected(boolean ussdConnected) {
		this.ussdConnected = ussdConnected;
		setUpdated(true);
	}

	/**
	 * @return the updated
	 */
	public boolean isUpdated() {
		return updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	/**
	 * @return the positiveCounter
	 */
	public String getPositiveCounter() {
		return positiveCounter;
	}

	/**
	 * @return the negativeCounter
	 */
	public String getNegativeCounter() {
		return ("".equals(negativeCounter) && !isSmsConnected()) ? "UP" : negativeCounter;
	}
}
