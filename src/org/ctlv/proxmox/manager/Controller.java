package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Controller {

	ProxmoxAPI api;
	public Controller(ProxmoxAPI api){
		this.api = api;
	}
	
	// migrer un conteneur du serveur "srcServer" vers le serveur "dstServer"
	public void migrateFromTo(String srcServer, String dstServer) throws LoginException, JSONException, IOException, InterruptedException  {
		LXC ctToMigrate = this.api.getCTs(srcServer).get(0);
		String ctID = ctToMigrate.getVmid();
		if (ctToMigrate.getStatus().equals("running")) {
			this.api.stopCT(srcServer, ctID);
			System.out.println("Stop container " + ctID);
			Thread.sleep(2000);
		}
		this.api.migrateCT(srcServer, ctID, dstServer);
		System.out.println("Migrate container " + ctID + " from " + srcServer + " to " + dstServer);
		
		Thread.sleep(2000);
		
		this.api.startCT(dstServer, String.valueOf(ctID));
		System.out.println("Restart container " + ctID + " on " + dstServer);
	}

	// arrêter le plus vieux conteneur sur le serveur "server"
	public void offLoad(String server) throws LoginException, JSONException, IOException {
		List<LXC> cts = this.getRunningCTs(this.api.getCTs(server));
		LXC ctToDrop = cts.get(0);
		long oldestID = Integer.valueOf(ctToDrop.getVmid());
		for (int i = 0; i < cts.size(); i++) {
			long currentID = Integer.valueOf(cts.get(i).getVmid());
			if (currentID == Constants.CT_BASE_ID) {
				oldestID = currentID;
				ctToDrop = cts.get(i);
				break;
			} 
			else if (currentID < oldestID) {
				oldestID = currentID;
				ctToDrop = cts.get(i);
			}
		}
		this.api.stopCT(server, String.valueOf(oldestID));
		System.out.println("Stop container " + oldestID + "on " + server);
	}
	
	@SuppressWarnings("null")
	private List<LXC> getRunningCTs(List<LXC> cts) {
		List<LXC> runningCTs = null;
		for (int i = 0; i < cts.size(); i++) {
			if (cts.get(i).getStatus().equals("running")) {
				runningCTs.add(cts.get(i));
			}
		}
		return runningCTs;
	}

}
