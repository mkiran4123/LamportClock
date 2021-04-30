package com.actor;

import java.util.ArrayList;
import java.util.Arrays;

import com.resource.TerminateProcesses;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.languageFeature.reflectiveCalls;

public class LamportMainActor extends AbstractActor{

	ArrayList<ActorRef> actorReflist = new ArrayList<ActorRef>();

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Integer.class, this::createProcesses)
				.match(TerminateProcesses.class, this::exitProcesses)
				.build();
	}

	private void createProcesses(int processCount) {
		for (int i = 0; i < processCount; i++) {
			ActorRef actorRef = getContext().actorOf(Props.create(LamportChildActor.class));
			actorReflist.add(actorRef);
		}
		ActorRef master = getContext().actorOf(Props.create(LamportChildActor.class));
		master.tell(actorReflist, getContext().getSelf());
	}
	
	private void exitProcesses(TerminateProcesses terminate) {
		ActorRef master = getContext().actorOf(Props.create(LamportChildActor.class));
		master.tell(terminate, getContext().getSelf());
	}
}
	
