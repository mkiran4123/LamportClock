package com.actor;

import java.io.IOException;

import com.resource.TerminateProcesses;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ActorSystemMain {

	public static void main(String[] args) {
		
		ActorSystem actorSystem = ActorSystem.create("MyActorSystem");
		ActorRef lamportMainActor = actorSystem.actorOf(Props.create(LamportMainActor.class));
		if (args.length>0) {
			try {
				lamportMainActor.tell(Integer.parseInt(args[0]), null);
			} catch (Exception e) {
				System.out.println("Enter only Integers");
			}
		}
		try {
			System.out.println(">>> Press ENTER to exit <<<");
			System.in.read();
		} catch (IOException ignored) {

		} finally {
			TerminateProcesses terminate = new TerminateProcesses();
			lamportMainActor.tell(terminate, null);
		}

	}

}
