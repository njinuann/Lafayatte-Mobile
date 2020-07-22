/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpos.iso.gui;

/**
 * allows for easy visualization of channel utilization. It shows messages
 * coming through in an 'Oscilloscope' style clickeable window.
 *
 * @see ISOMeter
 * @see ISOMsgPanel
 * @serial
 */
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOUtil;
import org.redlamp.core.MainClass;

public class ISOChannelPanel extends JPanel implements Observer {

	private static final long serialVersionUID = -8069489863639386589L;
	/**
	 * @serial
	 */
	ISOMeter meter;
	/**
	 * @serial
	 */
	DefaultListModel<ISOMsg> log;
	/**
	 * @serial
	 */
	String symbolicName;
	public static final int LOG_CAPACITY = 2000;

	public ISOChannelPanel(ISOChannel channel, String symbolicName) {
		super();
		this.symbolicName = symbolicName;
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createRaisedBevelBorder());
		log = new DefaultListModel<ISOMsg>();
		add(createCountersPanel());
		meter.setConnected(channel.isConnected());
		if (channel instanceof Observable) {
			((Observable) channel).addObserver(this);
		}
	}

	/**
	 * Unconnected constructor allows for instantiation of ISOChannelPanel without
	 * associated ISOChannel (that can be attached later)
	 */
	public ISOChannelPanel(String symbolicName) {
		super();
		this.symbolicName = symbolicName;
		setLayout(new FlowLayout());
		setBorder(BorderFactory.createRaisedBevelBorder());
		log = new DefaultListModel<ISOMsg>();
		add(createCountersPanel());
		meter.setConnected(false);
	}

	public final String getSymbolicName() {
		return symbolicName;
	}

	public final ListModel<ISOMsg> getLog() {
		return log;
	}

	public final void setLog(ListModel<ISOMsg> listModel) {
		log = (DefaultListModel<ISOMsg>) listModel;
	}

	public void update(Observable o, Object arg) {

		if (arg != null && arg instanceof ISOMsg) {
			ISOMsg m = (ISOMsg) arg;
			try {
				String mti = m.getMTI();
				int imti = Integer.parseInt(mti);

				if (m.isIncoming()) {
					meter.setValue(-imti, mti);
				} else {
					meter.setValue(imti, mti);
				}

				if (m.getMTI().startsWith("01") || m.getMTI().startsWith("02") || m.getMTI().startsWith("04")
						|| m.getMTI().startsWith("09") || m.getMTI().startsWith("12") || m.getMTI().startsWith("14")
						|| m.getMTI().startsWith("19") || m.getMTI().startsWith("9")) {
					log.addElement(m);
				}

				if (log.getSize() > LOG_CAPACITY) {
					log.remove(0);
				}

			} catch (Exception e) {
				meter.setValue(ISOMeter.mass, "ERROR");
			}
			meter.setValue(ISOMeter.mass);
		}
		if (o instanceof BaseChannel) {
			BaseChannel c = (BaseChannel) o;
			meter.setConnected(c.isConnected());
			int cnt[] = c.getCounters();
			try {
				meter.setPositiveCounter(ISOUtil.zeropad(Integer.toString(cnt[ISOChannel.TX % 1000000000]), 3));
				meter.setNegativeCounter(ISOUtil.zeropad(Integer.toString(cnt[ISOChannel.RX] % 1000000000), 3));
			} catch (ISOException e) {
			}
		} else if (o instanceof ISOServer) {
			final ISOServer server = (ISOServer) o;
			final Runnable updateIt = new Runnable() {
				public void run() {
					ISOUtil.sleep(250L);
					int active = server.getActiveConnections();
					meter.setConnected(active > 0);
					try {
						meter.setPositiveCounter(ISOUtil.zeropad(Integer.toString(active), 3));
					} catch (ISOException e) {
					}
					meter.repaint();
				}
			};
			MainClass.runner.execute(updateIt);
		} else {
			meter.setConnected(true);
		}
	}

	public ISOMeter getISOMeter() {
		return meter;
	}

	public void setProtectFields(int[] fields) {
	}

	public void setWipeFields(int[] fields) {
	}

	private JPanel createCountersPanel() {
		JPanel A = new JPanel() {
			private static final long serialVersionUID = 1175437215105556679L;

			public Insets getInsets() {
				return new Insets(10, 10, 10, 10);
			}
		};
		A.setLayout(new BorderLayout());
		meter = new ISOMeter(this);
		JLabel l = new JLabel(symbolicName);
		A.add(l, BorderLayout.NORTH);
		A.add(meter, BorderLayout.CENTER);
		// meter.start(); // ISOMeter has auto-start now
		return A;
	}
}
