package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;
import org.ctlv.proxmox.manager.Analyzer;
import org.ctlv.proxmox.manager.Controller;
import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class GeneratorMain {
	
	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		
		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;
		int ctID = 0;
		
		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();		
		ProxmoxAPI api = new ProxmoxAPI();
		Controller controller = new Controller(api);
		Analyzer analyser = new Analyzer(api, controller);
		
		Random rndServer = new Random(new Date().getTime());
		//Random rndRAM = new Random(new Date().getTime()); 
				
		while (ctID < 100) {
			
			// Liste les CTs de nos serveurs (7 & 8) dans notre hashmap
			for (int i = 7; i <= 8; i++) {
				String srv ="srv-px" + String.valueOf(i);
				List<LXC> cts = api.getCTs(srv);
				myCTsPerServer.put(srv, cts);
			}
			
			if (analyser.analyze(myCTsPerServer)) {  // Exemple de condition de l'arrêt de la génération de CTs
				// choisir un serveur aléatoirement avec les ratios spécifiés 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;
				
				// créer un contenaire sur ce serveur
				api.createCT(serverName, String.valueOf(baseID + ctID), Constants.CT_BASE_NAME + String.valueOf(ctID), 512);
				System.out.println("Create container " + String.valueOf(baseID + ctID) + " on " + serverName);

				// planifier la prochaine création
				int timeToWait = getNextEventExponential(lambda); // par exemple une loi expo d'une moyenne de 30sec
				ctID++;

				// attendre jusqu'au prochain évènement
				Thread.sleep(1000 * timeToWait);
			}
			else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
			}
		}
		
	}

}
