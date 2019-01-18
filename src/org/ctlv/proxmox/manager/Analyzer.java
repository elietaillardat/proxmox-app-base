package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Analyzer {
	ProxmoxAPI api;
	Controller controller;
	
	public Analyzer(ProxmoxAPI api, Controller controller) {
		this.api = api;
		this.controller = controller;
	}
	
	public Boolean analyze(Map<String, List<LXC>> myCTsPerServer) throws LoginException, JSONException, IOException, InterruptedException  {

		float memOnServer1 = 0;
		float memOnServer2 = 0;
		
		float memAllowedOnServer1 = this.api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD;
		float memAllowedOnServer2 = this.api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD;
		
		float memMigrationCT1 = this.api.getNode(Constants.SERVER1).getMemory_total() * Constants.MIGRATION_THRESHOLD;
		float memMigrationCT2 = this.api.getNode(Constants.SERVER2).getMemory_total() * Constants.MIGRATION_THRESHOLD;
		float memDropCT1 = this.api.getNode(Constants.SERVER1).getMemory_total() * Constants.DROPPING_THRESHOLD;
		float memDropCT2 = this.api.getNode(Constants.SERVER2).getMemory_total() * Constants.DROPPING_THRESHOLD;
		
		// Calculer la quantité de RAM utilisée par mes CTs sur chaque serveur
		List<LXC> serv1CTs = myCTsPerServer.get(Constants.SERVER1);
		for (LXC lxc : serv1CTs) {
			memOnServer1 += lxc.getMem();
		}
	
		List<LXC> serv2CTs = myCTsPerServer.get(Constants.SERVER2);
		for (LXC lxc : serv2CTs) {
			memOnServer2 += lxc.getMem();
		}
		
		// Mémoire autorisée sur chaque serveur
		float memRatioOnServer1 = 100f * (memOnServer1 / memAllowedOnServer1);		
		float memRatioOnServer2 = 100f * (memOnServer2 / memAllowedOnServer2);
		
		System.out.println("RAM max we can used on servers: " + Constants.MAX_THRESHOLD + " %");
		System.out.println("RAM used on " + Constants.SERVER1 + ": " + memRatioOnServer1*Constants.MAX_THRESHOLD + " % - Threshold at " + 100f * Constants.MIGRATION_THRESHOLD + "%");
		System.out.println("RAM used on " + Constants.SERVER2 + ": "+ memRatioOnServer2*Constants.MAX_THRESHOLD + " % - Threshold at " + 100f * Constants.DROPPING_THRESHOLD + "%");	

		// Analyse et Actions

		// cluster management
		if (memOnServer1 > memDropCT1) {
			this.controller.offLoad(Constants.SERVER1);
		}
		else if (memOnServer1 > memMigrationCT1) {
			this.controller.migrateFromTo(Constants.SERVER1, Constants.SERVER2);
		} 
		if (memOnServer2 > memDropCT2) {
			this.controller.offLoad(Constants.SERVER2);
		}
		else if (memOnServer2 > memMigrationCT2) {
			this.controller.migrateFromTo(Constants.SERVER2, Constants.SERVER1);
		}
		
		// to know if we can still create containers on servers
		return (memOnServer1 < memAllowedOnServer1 && memOnServer2 < memAllowedOnServer2);
	}

}
