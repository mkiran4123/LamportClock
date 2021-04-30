package com.resource;
import akka.actor.ActorRef;
import akka.actor.Address;
import java.io.Serializable;

import com.clock.LogicalClock;

public class RequestResource extends LamportMessages implements Serializable {

	private static final long serialVersionUID = 1L;

    public RequestResource(ActorRef senderActorReference, LogicalClock timestamp) {
        super(timestamp, senderActorReference);
    }
}
