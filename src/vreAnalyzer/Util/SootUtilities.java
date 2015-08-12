package vreAnalyzer.Util;

/* Utilities to use with Soot

 Copyright (c) 2001-2015 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PatchingChain;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Trap;
import soot.Type;
import soot.Unit;

import soot.UnitBox;

import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.util.Chain;
import soot.util.HashChain;

///////////////////////////////////////////////////////////////////
//// SootUtilities

/**
 This class consists of static utility methods for use with Soot

 @author Stephen Neuendorffer, Christopher Hylands
 @version $Id: SootUtilities.java 72874 2015-07-26 21:01:52Z cxh $
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SootUtilities {

	
	static boolean verbose = false;

	public static List copyFields(SootClass newClass, SootClass oldClass,Map<SootField,SootField> fieldMap) {
        List list = new LinkedList();
        Iterator fields = oldClass.getFields().iterator();
        SootField newField = null;
        while (fields.hasNext()) {
            SootField oldField = (SootField) fields.next();

            if (newClass.declaresFieldByName(oldField.getName())) {
                // FIXME
            	if(oldField.getType().equals(newClass.getFieldByName(oldField.getName()).getType())){
            		newField = new SootField(oldField.getName()+"_"+oldClass.getName(),oldField.getType(), oldField.getModifiers());
            		fieldMap.put(oldField, newField);
            	}
                
            }else{
            	newField = new SootField(oldField.getName(),oldField.getType(), oldField.getModifiers());
            }
            newClass.addField(newField);
        }

        return list;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make the given field final.  Anywhere where the the given
     *  field is used in the given class, inline the reference with
     *  the given value.  Anywhere where the given field is illegally
     *  defined in the given class, inline the definition to throw an
     *  exception.  This happens unless the given class is the
     *  defining class for the given field and the definition occurs
     *  within an initializer (for instance fields) or a static
     *  initializer (for static fields).

    /** Make the given field final.  Anywhere where the the given
     *  field is used in the given class, inline the reference with
     *  the given value.  Anywhere where the given field is illegally
     *  defined in the given class, inline the definition to throw an
     *  exception.  This happens unless the given class is the
     *  defining class for the given field and the definition occurs
     *  within an initializer (for instance fields) or a static
     *  initializer (for static fields).  Note that this is rather
     *  limited, since it is only really useful for constant values.
     *  In would be nice to specify a more complex expression to
     *  inline, but I'm not sure how to do it.
     */
    

    /** Copy a class */
    public static SootClass copyClass(SootClass oldClass, String newClassName) {
        //System.out.println("SootClass.copyClass(" + oldClass + ", "
        //                   + newClassName + ")");
        // Create the new Class
        SootClass newClass = new SootClass(newClassName,
                oldClass.getModifiers());
        
        try {
            Scene.v().addClass(newClass);
        } catch (RuntimeException runtime) {
            throw new RuntimeException("Perhaps you are calling the same "
                    + "transform twice?: " + runtime);
        }
        
        
        // Set the Superclass.
        newClass.setSuperclass(oldClass.getSuperclass());

        // Copy the interface declarations
        newClass.getInterfaces().addAll(oldClass.getInterfaces());

        // Copy the fields.
        _copyFields(newClass, oldClass);

        // Copy the methods.
        Iterator methods = oldClass.getMethods().iterator();

        while (methods.hasNext()) {
            SootMethod oldMethod = (SootMethod) methods.next();

            SootMethod newMethod = new SootMethod(oldMethod.getName(),
                    oldMethod.getParameterTypes(), oldMethod.getReturnType(),
                    oldMethod.getModifiers(), oldMethod.getExceptions());
            newClass.addMethod(newMethod);
            
            JimpleBody body = Jimple.v().newBody(newMethod);
            body.importBodyContentsFrom(oldMethod.retrieveActiveBody());
            
            newMethod.setActiveBody(body);
        }

        changeTypesOfFields(newClass, oldClass, newClass);
        changeTypesInMethods(newClass, oldClass, newClass);
        return newClass;
    }
    
    public static SootMethod methodClone(SootMethod oldMethod){

        SootMethod newMethod = new SootMethod(oldMethod.getName(),
                oldMethod.getParameterTypes(), oldMethod.getReturnType(),
                oldMethod.getModifiers(), oldMethod.getExceptions());

        JimpleBody body = Jimple.v().newBody(newMethod);
        body.importBodyContentsFrom(oldMethod.retrieveActiveBody());
        newMethod.setActiveBody(body);
        return newMethod;
    }
    
    /** Search through all the fields in the given class and if the
     *  field is of class oldClass, then change it to newClass.
     *  @param theClass The class containing fields to modify.
     *  @param oldClass The class to replace.
     *  @param newClass The new class.
     */

    public static void changeTypesOfFields(SootClass theClass,
            SootClass oldClass, SootClass newClass) {
        Iterator fields = theClass.getFields().snapshotIterator();

        while (fields.hasNext()) {
            SootField oldField = (SootField) fields.next();
            Type type = oldField.getType();

            //  System.out.println("field with type " + type);
            if (type instanceof RefType) {
                SootClass refClass = ((RefType) type).getSootClass();

                if (refClass == oldClass) {
                    oldField.setType(RefType.v(newClass));

                    // we have to do this seemingly useless
                    // thing, since the scene caches a pointer
                    // to the field based on it's parameter types.
                    theClass.removeField(oldField);
                    theClass.addField(oldField);
                } else if (refClass.getName().startsWith(oldClass.getName())) {
                    SootClass changeClass = _getInnerClassCopy(oldClass,
                            refClass, newClass);
                    oldField.setType(RefType.v(changeClass));

                    // we have to do this seemingly useless
                    // thing, since the scene caches a pointer
                    // to the field based on it's parameter types.
                    theClass.removeField(oldField);
                    theClass.addField(oldField);
                }
            }
        }
    }

    public static Map<SootField,SootField> addTypesOfFields(SootClass newClass,SootClass oldClass) {
    	 Map<SootField,SootField> fieldMaper = new HashMap<SootField,SootField>();
         Iterator oldfields = oldClass.getFields().iterator();

         while (oldfields.hasNext()) {
             SootField oldField = (SootField) oldfields.next();
             SootField oldFieldClone = new SootField(oldField.getName(),
                     oldField.getType(), oldField.getModifiers());
             SootField newField = null;
             if (newClass.declaresFieldByName(oldField.getName())) {
                 // FIXME
            	 newField = new SootField(oldField.getName()+"_"+oldClass.hashCode(),oldField.getType(), oldField.getModifiers());
            	 newClass.addField(newField);
            	 fieldMaper.put(oldField, newField);
            	 if(verbose){
            		 System.err.println("Add a map from\t"+oldField.toString()+"\tto\t"+newField.toString());
            	 }
            	 
             }
             else
            	 newClass.addField(oldFieldClone);
         }
         
    	Iterator fields = newClass.getFields().snapshotIterator();
        
        while (fields.hasNext()) {
            SootField existField = (SootField) fields.next();
            Type type = existField.getType();
            if(verbose)
            	System.out.println("field with type " + type);
            if (type instanceof RefType) {
                SootClass refClass = ((RefType) type).getSootClass();

                if (refClass == oldClass) {
                	
                	existField.setType(RefType.v(newClass));
                    // we have to do this seemingly useless
                    // thing, since the scene caches a pointer
                    // to the field based on it's parameter types.
                	newClass.removeField(existField);
                	newClass.addField(existField);
                } 
                else if (refClass.getName().startsWith(oldClass.getName())) {
                	
                    SootClass changeClass = _getInnerClassCopy(oldClass,
                            refClass, newClass);
                    existField.setType(RefType.v(changeClass));
                    // we have to do this seemingly useless
                    // thing, since the scene caches a pointer
                    // to the field based on it's parameter types.
                    newClass.removeField(existField);
                    newClass.addField(existField);
                }
            }
        }
        return fieldMaper;
    }

    /** Search through all the methods in the given class and change
     *  all references to the old class to references to the new class.
     *  This includes field references, type casts, this references,
     *  new object instantiations and method invocations.
     *  @param theClass The class containing methods to modify.
     *  @param oldClass The class to replace.
     *  @param newClass The new class.
     */
    public static void changeTypesInMethods(SootClass theClass,
            SootClass oldClass, SootClass newClass) {
    	if(verbose){
    		System.out.println("[vreAnalyzer] fixing references on " + theClass);
    		System.out.println("[vreAnalyzer] replacing " + oldClass + " with " + newClass);
    	}
        ArrayList methodList = new ArrayList(theClass.getMethods());

        for (Iterator methods = methodList.iterator(); methods.hasNext();) {
            SootMethod newMethod = (SootMethod) methods.next();

            // System.out.println("newMethod = " + newMethod.getSignature());
            Type returnType = newMethod.getReturnType();

            if (returnType instanceof RefType
                    && ((RefType) returnType).getSootClass() == oldClass) {
                newMethod.setReturnType(RefType.v(newClass));
            }

            List paramTypes = new LinkedList();

            for (Iterator oldParamTypes = newMethod.getParameterTypes()
                    .iterator(); oldParamTypes.hasNext();) {
                Type type = (Type) oldParamTypes.next();

                if (type instanceof RefType
                        && ((RefType) type).getSootClass() == oldClass) {
                    paramTypes.add(RefType.v(newClass));
                } else if (type instanceof RefType
                        && ((RefType) type).getSootClass().getName()
                        .startsWith(oldClass.getName())) {
                    SootClass changeClass = _getInnerClassCopy(oldClass,
                            ((RefType) type).getSootClass(), newClass);
                    paramTypes.add(RefType.v(changeClass));
                } else {
                    paramTypes.add(type);
                }
            }

            newMethod.setParameterTypes(paramTypes);

            // we have to do this seemingly useless
            // thing, since the scene caches a pointer
            // to the method based on it's parameter types.
            theClass.removeMethod(newMethod);
            theClass.addMethod(newMethod);

            Body newBody = newMethod.retrieveActiveBody();

            for (Iterator locals = newBody.getLocals().iterator(); locals
                    .hasNext();) {
                Local local = (Local) locals.next();
                Type type = local.getType();

                if (type instanceof RefType
                        && ((RefType) type).getSootClass() == oldClass) {
                    local.setType(RefType.v(newClass));
                }
            }

            Iterator j = newBody.getUnits().iterator();

            while (j.hasNext()) {
                Unit unit = (Unit) j.next();
                Iterator boxes = unit.getUseAndDefBoxes().iterator();
                //  System.out.println("unit = " + unit);

                while (boxes.hasNext()) {
                    ValueBox box = (ValueBox) boxes.next();
                    Value value = box.getValue();

                    if (value instanceof FieldRef) {
                        // Fix references to fields
                        FieldRef r = (FieldRef) value;
                        SootFieldRef fieldRef = r.getFieldRef();
                        if (fieldRef.type() instanceof RefType) {
                            RefType fieldType = (RefType) fieldRef.type();
                            SootClass fieldClass = fieldType.getSootClass();
                            if (fieldClass == oldClass) {
                                r.setFieldRef(Scene.v().makeFieldRef(
                                        fieldRef.declaringClass(),
                                        fieldRef.name(), RefType.v(newClass),
                                        fieldRef.isStatic()));
                            }
                            fieldRef = r.getFieldRef();
                        }

                        if (fieldRef.declaringClass() == oldClass) {
                            // We might also have a reference to a field
                            // which is not actually declared in the
                            // oldclass, in which case, we just fix up
                            // the ref to point to the new super class
                            r.setFieldRef(Scene.v().makeFieldRef(newClass,
                                    fieldRef.name(), fieldRef.type(),
                                    fieldRef.isStatic()));
                        } else if (fieldRef.declaringClass().getName()
                                .startsWith(oldClass.getName())) {
                            SootClass changeClass = _getInnerClassCopy(
                                    oldClass, r.getField().getDeclaringClass(),
                                    newClass);
                            r.setFieldRef(changeClass.getFieldByName(
                                    r.getField().getName()).makeRef());
                        }//  else if (r.getField().getDeclaringClass() == oldClass) {
                        //                             r.setFieldRef(
                        //                                     newClass.getFieldByName(
                        //                                             r.getField().getName()).makeRef());

                        //                             //   System.out.println("fieldRef = " +
                        //                             //              box.getValue());
                        //                         }

                    } else if (value instanceof CastExpr) {
                        // Fix casts
                        CastExpr r = (CastExpr) value;
                        Type type = r.getType();

                        if (type instanceof RefType) {
                            SootClass refClass = ((RefType) type)
                                    .getSootClass();

                            if (refClass == oldClass) {
                                r.setCastType(RefType.v(newClass));

                                // System.out.println("newValue = " +
                                //        box.getValue());
                            } else if (refClass.getName().startsWith(
                                    oldClass.getName())) {
                                SootClass changeClass = _getInnerClassCopy(
                                        oldClass, refClass, newClass);
                                r.setCastType(RefType.v(changeClass));
                            }
                        }
                    } else if (value instanceof ThisRef) {
                        // Fix references to 'this'
                        ThisRef r = (ThisRef) value;
                        Type type = r.getType();

                        if (type instanceof RefType
                                && ((RefType) type).getSootClass() == oldClass) {
                            box.setValue(Jimple.v().newThisRef(
                                    RefType.v(newClass)));
                        }
                    } else if (value instanceof ParameterRef) {
                        // Fix references to a parameter
                        ParameterRef r = (ParameterRef) value;
                        Type type = r.getType();

                        if (type instanceof RefType
                                && ((RefType) type).getSootClass() == oldClass) {
                            box.setValue(Jimple.v().newParameterRef(
                                    RefType.v(newClass), r.getIndex()));
                        }
                    } else if (value instanceof InvokeExpr) {
                        // Fix up the method invokes.
                        InvokeExpr r = (InvokeExpr) value;
                        SootMethodRef methodRef = r.getMethodRef();
                        if(verbose)
                        	System.out.println("[vreAnalyzer] invoke = " + r);

                        List newParameterTypes = new LinkedList();
                        for (Iterator i = methodRef.parameterTypes().iterator(); i
                                .hasNext();) {
                            Type type = (Type) i.next();
                            if (type instanceof RefType
                                    && ((RefType) type).getSootClass() == oldClass) {
                            	if(verbose)
                            		System.out.println("[vreAnalyzer] matchedParameter = "
                                        + newClass);
                                newParameterTypes.add(RefType.v(newClass));
                            } else if (type instanceof RefType
                                    && ((RefType) type).getSootClass()
                                    .getName()
                                    .startsWith(oldClass.getName())) {
                            	if(verbose)
                            		System.out.println("[vreAnalyzer] matchedParameter = "
                                        + newClass);
                                SootClass changeClass = _getInnerClassCopy(
                                        oldClass,
                                        ((RefType) type).getSootClass(),
                                        newClass);
                                newParameterTypes.add(RefType.v(changeClass));
                            } else {
                                newParameterTypes.add(type);
                            }

                        }

                        Type newReturnType = methodRef.returnType();
                        if (newReturnType instanceof RefType
                                && ((RefType) newReturnType).getSootClass() == oldClass) {
                            newReturnType = RefType.v(newClass);
                        }

                        // Update the parameter types and the return type.
                        methodRef = Scene.v().makeMethodRef(
                                methodRef.declaringClass(), methodRef.name(),
                                newParameterTypes, newReturnType,
                                methodRef.isStatic());
                        r.setMethodRef(methodRef);

                        if (methodRef.declaringClass() == oldClass) {
                            r.setMethodRef(Scene.v().makeMethodRef(newClass,
                                    methodRef.name(),
                                    methodRef.parameterTypes(),
                                    methodRef.returnType(),
                                    methodRef.isStatic()));
                            // System.out.println("newValue = " +
                            // box.getValue());
                        } else if (methodRef.declaringClass().getName()
                                .startsWith(oldClass.getName())) {
                            SootClass changeClass = _getInnerClassCopy(
                                    oldClass, methodRef.declaringClass(),
                                    newClass);
                            r.setMethodRef(Scene.v().makeMethodRef(changeClass,
                                    methodRef.name(),
                                    methodRef.parameterTypes(),
                                    methodRef.returnType(),
                                    methodRef.isStatic()));
                        }
                    } else if (value instanceof NewExpr) {
                        // Fix up the object creations.
                        NewExpr r = (NewExpr) value;

                        if (r.getBaseType().getSootClass() == oldClass) {
                            r.setBaseType(RefType.v(newClass));

                            //   System.out.println("newValue = " +
                            //           box.getValue());
                        } else if (r.getBaseType().getSootClass().getName()
                                .startsWith(oldClass.getName())) {
                            SootClass changeClass = _getInnerClassCopy(
                                    oldClass, r.getBaseType().getSootClass(),
                                    newClass);
                            r.setBaseType(RefType.v(changeClass));
                        }
                    }

                    //    System.out.println("value = " + value);
                    //   System.out.println("class = " +
                    //            value.getClass().getName());
                }
            }
        }
    }


    /** Create a new instance field with the given name
     *  and type and add it to the
     *  given class.  Add statements to the given body to initialize the
     *  field from the given local.
     */
    public static SootField createAndSetFieldFromLocal(JimpleBody body,
            Local local, SootClass theClass, Type type, String name) {
        return createAndSetFieldFromLocal(body, local, theClass, type, name,
                (Unit) body.getUnits().getLast());
    }

    /** Create a new instance field with the given name and type and
     *  add it to the given class.  Add statements to the given body
     *  after the given insertion point to initialize the field from
     *  the given local.
     */
    public static SootField createAndSetFieldFromLocal(JimpleBody body,
            Local local, SootClass theClass, Type type, String name,
            Unit insertPoint) {
        Chain units = body.getUnits();
        Local thisLocal = body.getThisLocal();

        Local castLocal;

        if (local.getType().equals(type)) {
            castLocal = local;
        } else {
            castLocal = Jimple.v().newLocal("local_" + name, type);
            body.getLocals().add(castLocal);

            // Cast the local to the type of the field.
            units.insertAfter(
                    Jimple.v().newAssignStmt(castLocal,
                            Jimple.v().newCastExpr(local, type)), insertPoint);
            insertPoint = (Unit) body.getUnits().getSuccOf(insertPoint);
        }

        // Create the new field if necessary
        SootField field;

        if (theClass.declaresFieldByName(name)) {
            field = theClass.getFieldByName(name);
        } else {
            field = new SootField(name, type, Modifier.PUBLIC);
            theClass.addField(field);
        }

        // Set the field.
        units.insertAfter(
                Jimple.v().newAssignStmt(
                        Jimple.v().newInstanceFieldRef(thisLocal,
                                field.makeRef()), castLocal), insertPoint);
        return field;
    }

    /** Create statements that correspond to a for loop and return
     *  them.  The returned list will incorporate the statements in
     *  the given list of initializer and body statements, and execute
     *  while the given conditional expression is true.
     */
    public static List createForLoopBefore(Body body, Unit insertPoint,
            List initializerList, List bodyList, Expr conditionalExpr) {
        List list = new LinkedList();
        Stmt bodyStart = (Stmt) bodyList.get(0);
        Stmt conditionalStmt = Jimple.v().newIfStmt(conditionalExpr, bodyStart);
        body.getUnits().insertBefore(initializerList, insertPoint);
        body.getUnits().insertBefore(Jimple.v().newGotoStmt(conditionalStmt),
                insertPoint);
        body.getUnits().insertBefore(bodyList, insertPoint);
        body.getUnits().insertBefore(conditionalStmt, insertPoint);
        return list;
    }

    /** Create a type with the same shape as the given shape type,
     *  containing elements of the type given by the given element
     *  type.  That is, if <i>shapeType</i> is a base type (a
     *  reference Type, or a native type), then return
     *  <i>elementType</i>.  If <i>shapeType</i> is an ArrayType, and
     *  <i>elementType</i> is a simple type, then return a new array
     *  type with the same number of dimensions as <i>shapeType</i>,
     *  and element type <i>elementType</i>.  If both are array types,
     *  then return a new array type with the sum of the number of
     *  dimensions, and the element type <i>elementType</i>.
     */
    public static Type createIsomorphicType(Type shapeType, Type elementType) {
        if (shapeType instanceof RefType || shapeType instanceof PrimType) {
            return elementType;
        } else if (shapeType instanceof ArrayType) {
            ArrayType arrayShapeType = (ArrayType) shapeType;

            if (elementType instanceof RefType
                    || elementType instanceof PrimType) {
                return ArrayType.v(elementType, arrayShapeType.numDimensions);
            } else if (elementType instanceof ArrayType) {
                ArrayType arrayElementType = (ArrayType) elementType;
                return ArrayType.v(arrayElementType.baseType,
                        arrayElementType.numDimensions
                        + arrayShapeType.numDimensions);
            }
        }

        throw new RuntimeException("Types for shape = " + shapeType
                + " and element = " + elementType
                + " must be arrays or base types.");
    }

    /** Create a new local variable in the given body, initialized
     *  before the given unit that refers to a Runtime exception with
     *  the given string message.
     */
    public static Local createRuntimeException(Body body, Unit unit,
            String string) {
        SootClass exceptionClass = Scene.v().getSootClass(
                "java.lang.RuntimeException");
        RefType exceptionType = RefType.v(exceptionClass);
        SootMethod initMethod = exceptionClass
                .getMethod("void <init>(java.lang.String)");

        Local local = Jimple.v().newLocal("exceptionLocal", exceptionType);
        body.getLocals().add(local);
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(local,
                        Jimple.v().newNewExpr(exceptionType)), unit);
        body.getUnits().insertBefore(
                Jimple.v()
                .newInvokeStmt(
                        Jimple.v().newSpecialInvokeExpr(local,
                                initMethod.makeRef(),
                                StringConstant.v(string))), unit);
        return local;
    }

   
    /** Copies the contents of the given Body into this one. */
    public static Map<Object, Object> setBodyContentsFrom(Body target, Body source)
    {

        /** The chain of locals for this Body. */
        Chain<Local> localChain = new HashChain<Local>();

        /** The chain of traps for this Body. */
        Chain<Trap> trapChain = new HashChain<Trap>();

        /** The chain of units for this Body. */
        PatchingChain<Unit> unitChain = new PatchingChain<Unit>(new HashChain<Unit>());

        HashMap<Object, Object> bindings = new HashMap<Object, Object>();

        {
	        // Clone units in body's statement list
	        for (Unit original : source.getUnits()) {
	            Unit copy = (Unit) original.clone();
	
	            copy.addAllTagsOf(original);
	            
	            // Add cloned unit to our unitChain.
	            unitChain.addLast(copy);
	            
	            // Build old <-> new map to be able to patch up references to other units
	            // within the cloned units. (these are still refering to the original
	            // unit objects).
	            bindings.put(original, copy);
	        }
        }
       
        
        {
	        // Clone trap units.
	        for (Trap original : source.getTraps()) {
	            Trap copy = (Trap) original.clone();
	
	            // Add cloned unit to our trap list.
	            trapChain.addLast(copy);
	            
	            
	            
	            // Store old <-> new mapping.
	            bindings.put(original, copy);
	        }
        }
        
        {
	        // Clone local units.
	        for (Local original : source.getLocals()) {
	            Local copy = (Local) original.clone();
	
	            // Add cloned unit to our trap list.
	            localChain.addLast(copy);
	            
	            
	            
	            // Build old <-> new mapping.
	            bindings.put(original, copy);
	        }
        }
       
        
        return bindings;
    }
   

   

    /** Given a Type object, return the java.lang.Class object that the
     *  type represents.
     */
    public static Class getClassForType(Type type)
            throws ClassNotFoundException {
        if (type instanceof RefType) {
            return Class.forName(((RefType) type).getSootClass().getName());
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            StringBuffer identifier = new StringBuffer();

            for (int i = 0; i < arrayType.numDimensions; i++) {
                identifier.append("[");
            }

            identifier.append(getClassForType(arrayType.baseType).getName());
            return Class.forName(identifier.toString());
        } else if (type instanceof ByteType) {
            return Byte.TYPE;
        } else if (type instanceof CharType) {
            return Character.TYPE;
        } else if (type instanceof DoubleType) {
            return Double.TYPE;
        } else if (type instanceof FloatType) {
            return Float.TYPE;
        } else if (type instanceof IntType) {
            return Integer.TYPE;
        } else if (type instanceof LongType) {
            return Long.TYPE;
        } else if (type instanceof ShortType) {
            return Short.TYPE;
        } else if (type instanceof BooleanType) {
            return Boolean.TYPE;
        } else {
            throw new RuntimeException("unknown type = " + type);
        }
    }

    /** Get the method in the given class that has the given name and will
     *  accept the given argument list.
     */
    public static SootMethod getMatchingMethod(SootClass theClass, String name,
            List args) {
        //        boolean found = false;
        SootMethod foundMethod = null;

        Iterator methods = theClass.getMethods().iterator();

        while (methods.hasNext()) {
            SootMethod method = (SootMethod) methods.next();

            //  System.out.println("checking method " + method);
            if (method.getName().equals(name)
                    && args.size() == method.getParameterCount()) {
                Iterator parameterTypes = method.getParameterTypes().iterator();
                Iterator arguments = args.iterator();
                boolean isEqual = true;

                while (parameterTypes.hasNext()) {
                    Type parameterType = (Type) parameterTypes.next();
                    Value argument = (Value) arguments.next();
                    Type argumentType = argument.getType();

                    if (argumentType != parameterType) {
                        // This is inefficient.  Full type merging is
                        // expensive and unnecessary.
                        isEqual = parameterType == argumentType.merge(
                                parameterType, Scene.v());
                    }

                    if (!isEqual) {
                        break;
                    }
                }

                // Coverity: Logically dead code.  found cannot be true.
                //                 if (isEqual && found) {
                //                     throw new RuntimeException("ambiguous method");
                //                 } else {
                //                     found = true;
                foundMethod = method;
                break;
                //                }
            }
        }

        return foundMethod;
    }

    /** Replace the invoke expression in the given statement in the
     * given body with the given value.  If the statement is an invoke
     * statement (without a return value) and the value is not an
     * invoke expression, then blindly replacing the invoke expression
     * is incorrect.  This method deals with this corner case by
     * removing the statement from the given body.
     */
    public static void replaceInvokeExpr(JimpleBody body, Stmt stmt, Value value) {
        if (stmt instanceof InvokeStmt && !(value instanceof InvokeExpr)) {
            body.getUnits().remove(stmt);
        } else {
            stmt.getInvokeExprBox().setValue(value);
        }
    }

    /** Get the method with the given name in the given class
     *  (or one of its super classes).
     *  If the method name is ambiguous or there is some other problem,
     *  then a RuntimeException is thrown that includes the names of
     *  all possible methods for that class at that level.
     */
    /**
     * It is unsafe for multiple same-name methods
     * @deprecated
     * @param theClass
     * @param name
     * @return
     */
    /**
    public static SootMethod searchForMethodByName(SootClass theClass,
            String name) {
        if (theClass == null) {
            throw new NullPointerException("searchForMethodByName(null, "
                    + name + ") called with a null first argument?");
        }
        SootClass previousClass = theClass;
        while (theClass != null) {
            if (theClass.declaresMethodByName(name)) {
                //System.out.println("found method " + name + " in " + theClass);
                try {
                    return theClass.getMethodByName(name);
                } catch (RuntimeException ex) {
                    // Print out what the possible methods are.
                    StringBuffer results = new StringBuffer("Methods are: ");
                    boolean commaNeeded = false;
                    Iterator methods = theClass.getMethods().iterator();
                    while (methods.hasNext()) {
                        SootMethod method = (SootMethod) methods.next();
                        if (commaNeeded) {
                            // Add a comma after the first method
                            results.append(", "
                            		+ "\n");
                        } else {
                            commaNeeded = true;
                        }
                        results.append(method.toString());
                    }
                    throw new RuntimeException("Failed to search \"" + theClass
                            + "\" for \"" + name + "\" possible " + "methods: "
                            + results.toString(), ex);
                }
            }

            theClass = theClass.getSuperclass();

            if (theClass != null) {
                previousClass = theClass;
                theClass.setLibraryClass();
            }
        }

        throw new RuntimeException("Method " + name + " not found in class "
                + previousClass);
    }
     **/
   

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Copy all the fields into the given class from the given old class.
     *  @return A list of fields in the new class that were created whose
     *  names collide with fields already there.
     */
    private static List _copyFields(SootClass newClass, SootClass oldClass) {
        List list = new LinkedList();
        Iterator fields = oldClass.getFields().iterator();

        while (fields.hasNext()) {
            SootField oldField = (SootField) fields.next();

            if (newClass.declaresFieldByName(oldField.getName())) {
                // FIXME
                throw new RuntimeException("[vreAnalyzer] Field " + oldField
                        + " cannot be folded into " + newClass
                        + " because its name is the same as "
                        + newClass.getFieldByName(oldField.getName()));
            }

            SootField newField = new SootField(oldField.getName(),
                    oldField.getType(), oldField.getModifiers());
            newClass.addField(newField);
        }

        return list;
    }

    private static SootClass _getInnerClassCopy(SootClass oldOuterClass,
            SootClass oldInnerClass, SootClass newOuterClass) {
        String oldInnerClassName = oldInnerClass.getName();
        String oldInnerClassSpecifier = oldInnerClassName
                .substring(oldOuterClass.getName().length());

        //System.out.println("oldInnerClassSpecifier = " +
        //        oldInnerClassSpecifier);
        String newInnerClassName = newOuterClass.getName()
                + oldInnerClassSpecifier;
        SootClass newInnerClass;

        if (Scene.v().containsClass(newInnerClassName)) {
            newInnerClass = Scene.v().getSootClass(newInnerClassName);
        } else {
            oldInnerClass.setLibraryClass();

            //   System.out.println("copying "+ oldInnerClass +
            //           " to " + newInnerClassName);
            newInnerClass = copyClass(oldInnerClass, newInnerClassName);
            newInnerClass.setApplicationClass();
        }

        changeTypesOfFields(newInnerClass, oldOuterClass, newOuterClass);
        changeTypesInMethods(newInnerClass, oldOuterClass, newOuterClass);
        return newInnerClass;
    }
}
