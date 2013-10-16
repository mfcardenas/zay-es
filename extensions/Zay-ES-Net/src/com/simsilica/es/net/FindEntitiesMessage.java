/*
 * $Id$
 *
 * Copyright (c) 2013 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.es.net;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.simsilica.es.ComponentFilter;
import java.util.Arrays;


/**
 *
 *  @author    Paul Speed
 */
@Serializable
public class FindEntitiesMessage extends AbstractMessage {

    private int requestId;
    private ComponentFilter filter;
    private Class[] componentTypes;

    public FindEntitiesMessage() {
    }
    
    public FindEntitiesMessage( int requestId, ComponentFilter filter, Class... components ) {
        this.requestId = requestId;
        this.filter = filter;
        this.componentTypes = components;
    }
 
    public int getRequestId() {
        return requestId;
    }
 
    public ComponentFilter getFilter() {
        return filter;
    }
    
    public Class[] getComponentTypes() {
        return componentTypes;
    }
 
    @Override   
    public String toString() {
        return "FindEntitiesMessage[" + requestId + ", " + filter + ", " + Arrays.asList(componentTypes) + "]"; 
    }
}

