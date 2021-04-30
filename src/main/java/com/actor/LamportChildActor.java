package com.actor;

import java.util.ArrayList;
import java.util.HashMap;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.resource.OwnResource;
import com.resource.ReleaseResource;
import com.resource.RequestResource;
import com.resource.TerminateProcesses;
import com.clock.LogicalClock;



public class LamportChildActor extends AbstractActor{
	
	ArrayList<ActorRef> actorReferenceList = new ArrayList<ActorRef>();
	public boolean isResourceLocked = false;
	HashMap<ActorRef, Integer> queue = new HashMap<ActorRef, Integer>();
	LogicalClock clockInstance = new LogicalClock();
	HashMap<ActorRef, LogicalClock> logMapInstance = new HashMap<ActorRef, LogicalClock>();
	

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ArrayList.class, this::initiateProcesses)
				.match(RequestResource.class, this::requestResource)
				.match(OwnResource.class, this::ownResource)
				.match(ReleaseResource.class, this::releaseResource)
				.match(TerminateProcesses.class, this::terminateProcess)
				.build();
	}

	private void initiateProcesses(ArrayList<ActorRef> actorReference) {
		clockInstance.increment();
		actorReferenceList = actorReference;
		for (ActorRef mainActor : actorReferenceList) {	
			logMapInstance.put(mainActor, clockInstance);
			for(ActorRef secondaryActor : actorReferenceList) {
				if(!mainActor.equals(secondaryActor)) {
					RequestResource requestObj = new RequestResource(secondaryActor, clockInstance.clone());
					getContext().getSystem().log().info("{} ACQUIRING resource with clock of {} ", mainActor.hashCode(), clockInstance.getClock() );
					getContext().getSelf().tell(requestObj, mainActor);
				}
			}
		}
	}
	
	private void requestResource(RequestResource requestObject) {			
		logMapInstance.put(requestObject.senderActorReference(), logMapInstance.get(requestObject.senderActorReference()).sync(logMapInstance.get(getSender())));
		OwnResource ack = new OwnResource(getSender(), logMapInstance.get(requestObject.senderActorReference()));
		getContext().getSelf().tell(ack, requestObject.senderActorReference());
	}
	
	
	private void ownResource(OwnResource ownResourceObject) {
		if (ownResourceObject != null && queue.get(ownResourceObject.senderActorReference()) == null) {
			queue.put(ownResourceObject.senderActorReference(), 1);
			}
		else {
			queue.put(ownResourceObject.senderActorReference(), queue.get(ownResourceObject.senderActorReference()) + 1);
		}
		if (queue.get(ownResourceObject.senderActorReference()) == actorReferenceList.size()-1)
			isResourceLocked = true;
			if (isResourceLocked) {
				getContext().getSystem().log().info("{} OWNED resource with clock of {} ", ownResourceObject.senderActorReference().hashCode(), logMapInstance.get(ownResourceObject.senderActorReference()).getClock());
			}
			isResourceLocked = false;
			queue.put(ownResourceObject.senderActorReference(), 0);
			getContext().getSystem().log().info("{} RELEASED resource with clock of {} ", ownResourceObject.senderActorReference().hashCode(), logMapInstance.get(ownResourceObject.senderActorReference()).getClock());
			ReleaseResource release = new ReleaseResource(null, logMapInstance.get(ownResourceObject.senderActorReference()));
			getContext().getSelf().tell(release, ownResourceObject.senderActorReference());	
	}
	
	
	private void releaseResource(ReleaseResource releseObject) {	
		for (ActorRef mainActor : actorReferenceList) {
			if(mainActor != getSender()) {
					LogicalClock c = logMapInstance.get(getSender());
					c.increment();
					logMapInstance.put(getSender(), c);
					RequestResource requestObject= new RequestResource(mainActor, c);
					getContext().getSystem().log().info("{} ACQUIRING resource with clock of {} ", getSender().hashCode(),mainActor.hashCode(), c.getClock());
					getContext().getSelf().tell(requestObject, getSender());
			}
		}

	}
	
	private void terminateProcess(TerminateProcesses e) {
		for (ActorRef actor : actorReferenceList) {
			System.out.println("Hello >>>>");
				LogicalClock c = logMapInstance.get(getSender());
				c.increment();
				logMapInstance.remove(actor);
				getContext().getSystem().log().info("{} RELEASED resource due to TERMINATION with clock of {} ", getSender().hashCode(), c.getClock());
		}
		getContext().getSystem().terminate();
	}
}


