package com.agileapes.nemo.action;

import com.agileapes.nemo.action.impl.ActionContext;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/13/13, 12:00 PM)
 */
public interface ActionContextAware {

    void setActionContext(ActionContext actionContext);

}
