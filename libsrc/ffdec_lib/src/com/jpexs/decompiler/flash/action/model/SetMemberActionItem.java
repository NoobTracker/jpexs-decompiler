/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class SetMemberActionItem extends ActionItem implements SetTypeActionItem {

    public GraphTargetItem object;

    public GraphTargetItem objectName;

    private int tempRegister = -1;

    public GraphTargetItem compoundValue;

    public String compoundOperator;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(object);
        visitor.visit(objectName);
        visitor.visit(value);
    }

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    @Override
    public void setValue(GraphTargetItem value) {
        this.value = value;
    }

    @Override
    public int getTempRegister() {
        return tempRegister;
    }

    @Override
    public void setTempRegister(int tempRegister) {
        this.tempRegister = tempRegister;
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    public SetMemberActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, GraphTargetItem objectName, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_ASSIGMENT, value);
        this.object = object;
        this.objectName = objectName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (((object instanceof DirectValueActionItem) && (((DirectValueActionItem) object).value instanceof Long))) {
            writer.append("(");
            object.toString(writer, localData);
            writer.append(")");
        } else {
            object.toString(writer, localData);
        }

        if ((!(objectName instanceof DirectValueActionItem)) || (!((DirectValueActionItem) objectName).isString()) || (!IdentifiersDeobfuscation.isValidName(false, ((DirectValueActionItem) objectName).toStringNoQuotes(localData)))) {
            writer.append("[");
            objectName.toString(writer, localData);
            writer.append("]");
        } else {
            writer.append(".");
            stripQuotes(objectName, localData, writer);
        }
        if (compoundOperator != null) {
            writer.append(" ");
            writer.append(compoundOperator);
            writer.append("= ");
            return compoundValue.toString(writer, localData);
        }
        writer.append(" = ");
        return value.toString(writer, localData);

    }

    @Override
    public GraphTargetItem getObject() {
        return new GetMemberActionItem(getSrc(), getLineStartItem(), object, objectName);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(object.getNeededSources());
        ret.addAll(objectName.getNeededSources());
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        int tmpReg = asGenerator.getTempRegister(localData);
        try {
            return toSourceMerge(localData, generator, object, objectName, value, new ActionStoreRegister(tmpReg), new ActionSetMember(), new ActionPush(new RegisterNumber(tmpReg)));
        } finally {
            asGenerator.releaseTempRegister(localData, tmpReg);
        }
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, object, objectName, value, new ActionSetMember());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.object);
        hash = 23 * hash + Objects.hashCode(this.objectName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SetMemberActionItem other = (SetMemberActionItem) obj;
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (!Objects.equals(this.objectName, other.objectName)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SetMemberActionItem other = (SetMemberActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.object, other.object)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.objectName, other.objectName)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public GraphTargetItem getCompoundValue() {
        return compoundValue;
    }

    @Override
    public void setCompoundValue(GraphTargetItem value) {
        this.compoundValue = value;
    }

    @Override
    public void setCompoundOperator(String operator) {
        this.compoundOperator = operator;
    }

    @Override
    public String getCompoundOperator() {
        return compoundOperator;
    }

}
