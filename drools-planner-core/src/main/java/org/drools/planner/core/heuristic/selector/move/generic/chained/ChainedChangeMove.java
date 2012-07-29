/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core.heuristic.selector.move.generic.chained;

import org.apache.commons.lang.ObjectUtils;
import org.drools.planner.core.domain.variable.PlanningVariableDescriptor;
import org.drools.planner.core.heuristic.selector.move.generic.ChangeMove;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.director.ScoreDirector;

public class ChainedChangeMove extends ChangeMove {

    public ChainedChangeMove(Object entity, PlanningVariableDescriptor variableDescriptor, Object toPlanningValue) {
        super(entity, variableDescriptor, toPlanningValue);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return super.isMoveDoable(scoreDirector)
                && !ObjectUtils.equals(entity, toPlanningValue);
    }

    @Override
    public Move createUndoMove(ScoreDirector scoreDirector) {
        Object oldPlanningValue = variableDescriptor.getValue(entity);
        return new ChainedChangeMove(entity, variableDescriptor, oldPlanningValue);
    }

    @Override
    public void doMove(ScoreDirector scoreDirector) {
        Object oldPlanningValue = variableDescriptor.getValue(entity);
        Object oldTrailingEntity = scoreDirector.getTrailingEntity(variableDescriptor, entity);
        // If chaining == true then toPlanningValue == null guarantees an uninitialized entity
        Object newTrailingEntity = toPlanningValue == null ? null
                : scoreDirector.getTrailingEntity(variableDescriptor, toPlanningValue);

        // Close the old chain
        if (oldTrailingEntity != null) {
            scoreDirector.beforeVariableChanged(oldTrailingEntity, variableDescriptor.getVariableName());
            variableDescriptor.setValue(oldTrailingEntity, oldPlanningValue);
            scoreDirector.afterVariableChanged(oldTrailingEntity, variableDescriptor.getVariableName());
        }

        // Change the entity
        scoreDirector.beforeVariableChanged(entity, variableDescriptor.getVariableName());
        variableDescriptor.setValue(entity, toPlanningValue);
        scoreDirector.afterVariableChanged(entity, variableDescriptor.getVariableName());

        // Reroute the new chain
        if (newTrailingEntity != null) {
            scoreDirector.beforeVariableChanged(newTrailingEntity, variableDescriptor.getVariableName());
            variableDescriptor.setValue(newTrailingEntity, entity);
            scoreDirector.afterVariableChanged(newTrailingEntity, variableDescriptor.getVariableName());
        }
    }

}
