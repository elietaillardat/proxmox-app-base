package org.ctlv.proxmox.tester;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;
import org.ctlv.proxmox.api.Constants;

public class Main {

	public static void main(String[] args) throws LoginException, JSONException, IOException {

		ProxmoxAPI api = new ProxmoxAPI();		
		Node srv7 = api.getNode(Constants.SERVER1);
		Node srv8 = api.getNode(Constants.SERVER2);

		// Collect informations about a Proxmox server
		System.out.println("Proxmox server informations:");
		
		float cpuUsageSrv7 = 100*srv7.getCpu();
		float cpuUsageSrv8 = 100*srv8.getCpu();
		System.out.println(Constants.SERVER1 + " cpu usage: " + cpuUsageSrv7 + "%");
		System.out.println(Constants.SERVER2 + " cpu usage: " + cpuUsageSrv8 + "%");
		
		float diskUsageSrv7 = srv7.getRootfs_used();
		float diskTotalSrv7 = srv7.getRootfs_total();
		float diskRatioSrv7 = 100 * diskUsageSrv7 / diskTotalSrv7;
		System.out.println(Constants.SERVER1 + " disk usage: " + diskRatioSrv7 + "%");
		
		float memoryUsageSrv7 = srv7.getMemory_used();
		float memoryTotalSrv7 = srv7.getMemory_total();
		float memoryRatioSrv7 = 100 * memoryUsageSrv7 / memoryTotalSrv7;
		System.out.println(Constants.SERVER1 + " memory usage: " + memoryRatioSrv7 + "%");
		
		// Create and start containers
		System.out.println("\nCreate and start containers...");
		//api.createCT(Constants.SERVER1, String.valueOf(Constants.CT_BASE_ID), Constants.CT_BASE_NAME + "1", 512);
		//api.startCT(Constants.SERVER1, String.valueOf(Constants.CT_BASE_ID));
		LXC ct1 = api.getCT(Constants.SERVER1, String.valueOf(Constants.CT_BASE_ID));
		
		// Collect informations about a container
		System.out.println("\nContainer informations:");

		float cpuUsageCt1 = 100*ct1.getCpu();
		System.out.println(ct1.getName() + " cpu usage: " + cpuUsageCt1 + "%");
		
		float diskUsageCt1 = ct1.getDisk();
		float diskTotalCt1 = ct1.getMaxdisk();
		float diskRatioCt1 = 100 * diskUsageCt1 / diskTotalCt1;
		System.out.println(ct1.getName() + " disk usage: " + diskRatioCt1 + "%");
		
		float memoryUsageCt1  = ct1.getMem();
		float memoryTotalCt1 = ct1.getMaxmem();
		float memoryRatioCt1 = 100 * memoryUsageCt1 / memoryTotalCt1;
		System.out.println(ct1.getName() + " memory usage: " + memoryRatioCt1 + "%");
		
		// Host server name
		for (int i=1; i<=10; i++) {
			String srv ="srv-px"+i;
			List<LXC> cts = api.getCTs(srv);
			for (LXC lxc : cts) {
				if (lxc.getName().equals(ct1.getName())) {
					System.out.println("Host server: " + srv);
					break;
				}
			}
		}

		// Stopper/Détruire les CTs de nos serveurs
		for (int i = 7; i <= 8; i++) {
			String srv ="srv-px" + i;
			List<LXC> cts = api.getCTs(srv);
			for (LXC lxc : cts) {
				if (lxc.getStatus().equals("running")) api.stopCT(srv, lxc.getVmid());
				//if (lxc.getStatus().equals("stopped")) api.deleteCT(srv, lxc.getVmid());
			}
		}
		
	}

}
