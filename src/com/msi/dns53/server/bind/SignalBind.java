package com.msi.dns53.server.bind;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;

import com.msi.dns53.model.DNS53HostedZone;
import com.msi.dns53.server.AccessDB;
import com.msi.dns53.server.AccessMySQL;
import com.msi.tough.core.Appctx;


public class SignalBind {
	private final static Logger logger =  Appctx.getLogger(SignalBind.class.getName());
	private static final int DELAY_SECS = 60;
	private String userName;
	private String password;
	private String dnsIp;

	private ScheduledExecutorService scheduler;
	private static SignalBind main;

	public SignalBind() {

    }

	private SignalBind(String userName, String passwd, String hostname){
		this.userName = userName;
		this.password = passwd;
		this.dnsIp = hostname;
	}

	public static SignalBind getInstance(){
		return main;
	}

	public static SignalBind initialize(String userName, String passwd, String hostname){
		main = new SignalBind(userName, passwd, hostname);
		return main;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDnsIp() {
		return dnsIp;
	}

	public void setDnsIp(String dnsIp) {
		this.dnsIp = dnsIp;
	}

	public void process(){
		this.scheduler =
				Executors.newScheduledThreadPool(1);
		final Runnable restarter = new Runnable() {
			public void run() {
				try {
					AccessDB sqlaccess = AccessMySQL.getInstance();
					boolean restartNeeded = sqlaccess.pendingChangesExist();

					if(restartNeeded){
						logger.debug("Pending changes exist; rewriting bind configuration file.");
						List<DNS53HostedZone> hostedZones = sqlaccess.getAllHostedZones();

						ConfigurationWriter writer = new ConfigurationWriter("/etc/named.conf");
						String content = NamedConfGenerator.getDefaultNamedConf();
						for(DNS53HostedZone hz : hostedZones){
							content += NamedConfGenerator.generateZoneConf(hz.getName(),
									hz.getTableName(), "localhost", userName, password);
						}
						writer.write(content);
						writer.close();

						logger.debug("Preparing to restart bind in order to synchronize changes.");
						Process process = new ProcessBuilder("/bin/bash", "-c", "sudo service named reload").start();
						InputStream is = process.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);
						String line = null;
						while ((line = br.readLine()) != null) {
							logger.debug("Restart named result:" + line);
						}

						logger.debug("Updating status: bind is in sync.");
						sqlaccess.updateChanges();
					} else{
						logger.debug("No pending changes. Will check again in "
						        + DELAY_SECS + " seconds.");
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		scheduler.scheduleAtFixedRate(restarter, 0, DELAY_SECS, SECONDS);
	}

	public void close(){
		this.scheduler.shutdown();
	}
}

