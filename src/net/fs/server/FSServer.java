// Copyright (c) 2015 D1SM.net

// Copyright (c) 2015 D1SM.net

package net.fs.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.fs.rudp.ConnectionProcessor;
import net.fs.rudp.Route;
import net.fs.utils.MLog;

public class FSServer {

	ConnectionProcessor imTunnelProcessor;

	Route route_udp, route_tcp, route;

	int routePort = 150;

	static FSServer udpServer;

	String systemName = System.getProperty("os.name").toLowerCase();

	public static void main(String[] args) throws Exception {
		FSServer fs = new FSServer();
	}

	static FSServer get() {
		return udpServer;
	}

	public FSServer() throws Exception {
		MLog.info("System Name: " + systemName);
		udpServer = this;
		final MapTunnelProcessor mp = new MapTunnelProcessor();

		String port_s = readFileData("./cnf/listen_port");
		if (port_s != null && !port_s.trim().equals("")) {
			port_s = port_s.replaceAll("\n", "").replaceAll("\r", "");
			routePort = Integer.parseInt(port_s);
		}
		route_udp = new Route(mp.getClass().getName(), (short) routePort, Route.mode_server, false);
		if (systemName.equals("linux")) {
			startFirewall_linux();
			setFireWall_linux_udp();
		}

		Route.es.execute(new Runnable() {

			@Override
			public void run() {
				try {
					route_tcp = new Route(mp.getClass().getName(), (short) routePort, Route.mode_server, true);
					if (systemName.equals("linux")) {
						setFireWall_linux_tcp();
					}
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		});

	}

	void startFirewall_linux() {
		String cmd1 = "service iptables start";
		runCommand(cmd1);
	}

	void setFireWall_linux_udp() {
		cleanUdpTunRule();
		String cmd2 = "iptables -I INPUT -p udp --dport " + routePort + " -j ACCEPT"
				+ " -m comment --comment udptun_fs_server";
		runCommand(cmd2);
	}

	void cleanUdpTunRule() {
		while (true) {
			int row = getRow("udptun_fs_server");
			if (row > 0) {
				// MLog.println("删除行 "+row);
				String cmd = "iptables -D INPUT " + row;
				runCommand(cmd);
			} else {
				break;
			}
		}
	}

	void setFireWall_linux_tcp() {
		cleanTcpTunRule();
		String cmd2 = "iptables -I INPUT -p tcp --dport " + routePort + " -j DROP"
				+ " -m comment --comment tcptun_fs_server ";
		runCommand(cmd2);

	}

	void cleanTcpTunRule() {
		while (true) {
			int row = getRow("tcptun_fs_server");
			if (row > 0) {
				// MLog.println("删除行 "+row);
				String cmd = "iptables -D INPUT " + row;
				runCommand(cmd);
			} else {
				break;
			}
		}
	}

	int getRow(String name) {
		int row_delect = -1;
		String cme_list_rule = "iptables -L -n --line-number";
		// String [] cmd={"netsh","advfirewall set allprofiles state on"};
		Thread errorReadThread = null;
		try {
			final Process p = Runtime.getRuntime().exec(cme_list_rule, null);

			errorReadThread = new Thread() {
				public void run() {
					InputStream is = p.getErrorStream();
					BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
					while (true) {
						String line;
						try {
							line = localBufferedReader.readLine();
							if (line == null) {
								break;
							} else {
								// System.out.println("erroraaa "+line);
							}
						} catch (IOException e) {
							e.printStackTrace();
							// error();
							break;
						}
					}
				}
			};
			errorReadThread.start();

			InputStream is = p.getInputStream();
			BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line;
				try {
					line = localBufferedReader.readLine();
					// System.out.println("standaaa "+line);
					if (line == null) {
						break;
					} else {
						if (line.contains(name)) {
							int index = line.indexOf("   ");
							if (index > 0) {
								String n = line.substring(0, index);
								try {
									if (row_delect < 0) {
										// System.out.println("standaaabbb
										// "+line);
										row_delect = Integer.parseInt(n);
									}
								} catch (Exception e) {

								}
							}
						}
						;
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}

			errorReadThread.join();
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			// error();
		}
		return row_delect;
	}

	void runCommand(String command) {
		Thread standReadThread = null;
		Thread errorReadThread = null;
		try {
			final Process p = Runtime.getRuntime().exec(command, null);
			standReadThread = new Thread() {
				public void run() {
					InputStream is = p.getInputStream();
					BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
					while (true) {
						String line;
						try {
							line = localBufferedReader.readLine();
							// System.out.println("stand "+line);
							if (line == null) {
								break;
							}
						} catch (IOException e) {
							e.printStackTrace();
							break;
						}
					}
				}
			};
			standReadThread.start();

			errorReadThread = new Thread() {
				public void run() {
					InputStream is = p.getErrorStream();
					BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
					while (true) {
						String line;
						try {
							line = localBufferedReader.readLine();
							if (line == null) {
								break;
							} else {
								// System.out.println("error "+line);
							}
						} catch (IOException e) {
							e.printStackTrace();
							// error();
							break;
						}
					}
				}
			};
			errorReadThread.start();
			standReadThread.join();
			errorReadThread.join();
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			// error();
		}
	}

	String readFileData(String path) {
		String content = null;
		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			File file = new File(path);
			fis = new FileInputStream(file);
			dis = new DataInputStream(fis);
			byte[] data = new byte[(int) file.length()];
			dis.readFully(data);
			content = new String(data, "utf-8");
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

}
