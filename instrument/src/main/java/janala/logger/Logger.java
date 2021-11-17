package janala.logger;

public interface Logger {
  public void LDC(int iid, int mid, int c);

  public void LDC(int iid, int mid, long c);

  public void LDC(int iid, int mid, float c);

  public void LDC(int iid, int mid, double c);

  public void LDC(int iid, int mid, String c);

  public void LDC(int iid, int mid, Object c);

  public void IINC(int iid, int mid, int var, int increment);

  public void MULTIANEWARRAY(int iid, int mid, String desc, int dims);

  public void LOOKUPSWITCH(int iid, int mid, int dflt, int[] keys, int[] labels);

  public void TABLESWITCH(int iid, int mid, int min, int max, int dflt, int[] labels);

  public void IFEQ(String fileName, int iid, int mid, int label);

  public void IFNE(String fileName, int iid, int mid, int label);

  public void IFLT(String fileName, int iid, int mid, int label);

  public void IFGE(String fileName, int iid, int mid, int label);

  public void IFGT(String fileName, int iid, int mid, int label);

  public void IFLE(String fileName, int iid, int mid, int label);

  public void IF_ICMPEQ(String fileName, int iid, int mid, int label);

  public void IF_ICMPNE(String fileName, int iid, int mid, int label);

  public void IF_ICMPLT(String fileName, int iid, int mid, int label);

  public void IF_ICMPGE(String fileName, int iid, int mid, int label);

  public void IF_ICMPGT(String fileName, int iid, int mid, int label);

  public void IF_ICMPLE(String fileName, int iid, int mid, int label);

  public void IF_ACMPEQ(String fileName, int iid, int mid, int label);

  public void IF_ACMPNE(String fileName, int iid, int mid, int label);

  public void GOTO(int iid, int mid, int label);

  public void JSR(int iid, int mid, int label);

  public void IFNULL(String fileName, int iid, int mid, int label);

  public void IFNONNULL(String fileName, int iid, int mid, int label);

  public void INVOKEVIRTUAL(int iid, int mid, String owner, String name, String desc);

  public void INVOKESPECIAL(int iid, int mid, String owner, String name, String desc);

  public void INVOKESTATIC(int iid, int mid, String owner, String name, String desc);

  public void INVOKEINTERFACE(int iid, int mid, String owner, String name, String desc);

  public void HEAPLOAD(int iid, int mid, int objectId, String field);

  public void GETSTATIC(int iid, int mid, int cIdx, int fIdx, String desc);

  public void PUTSTATIC(int iid, int mid, int cIdx, int fIdx, String desc);

  public void GETFIELD(int iid, int mid, int cIdx, int fIdx, String desc);

  public void PUTFIELD(int iid, int mid, int cIdx, int fIdx, String desc);

  public void NEW(int iid, int mid, String type, int cIdx);

  public void ANEWARRAY(int iid, int mid, String type);

  public void CHECKCAST(int iid, int mid, String type);

  public void INSTANCEOF(int iid, int mid, String type);

  public void BIPUSH(int iid, int mid, int value);

  public void SIPUSH(int iid, int mid, int value);

  public void NEWARRAY(int iid, int mid);

  public void ILOAD(int iid, int mid, int var);

  public void LLOAD(int iid, int mid, int var);

  public void FLOAD(int iid, int mid, int var);

  public void DLOAD(int iid, int mid, int var);

  public void ALOAD(int iid, int mid, int var);

  public void ISTORE(int iid, int mid, int var);

  public void LSTORE(int iid, int mid, int var);

  public void FSTORE(int iid, int mid, int var);

  public void DSTORE(int iid, int mid, int var);

  public void ASTORE(int iid, int mid, int var);

  public void RET(int iid, int mid, int var);

  public void NOP(int iid, int mid);

  public void ACONST_NULL(int iid, int mid);

  public void ICONST_M1(int iid, int mid);

  public void ICONST_0(int iid, int mid);

  public void ICONST_1(int iid, int mid);

  public void ICONST_2(int iid, int mid);

  public void ICONST_3(int iid, int mid);

  public void ICONST_4(int iid, int mid);

  public void ICONST_5(int iid, int mid);

  public void LCONST_0(int iid, int mid);

  public void LCONST_1(int iid, int mid);

  public void FCONST_0(int iid, int mid);

  public void FCONST_1(int iid, int mid);

  public void FCONST_2(int iid, int mid);

  public void DCONST_0(int iid, int mid);

  public void DCONST_1(int iid, int mid);

  public void IALOAD(int iid, int mid);

  public void LALOAD(int iid, int mid);

  public void FALOAD(int iid, int mid);

  public void DALOAD(int iid, int mid);

  public void AALOAD(int iid, int mid);

  public void BALOAD(int iid, int mid);

  public void CALOAD(int iid, int mid);

  public void SALOAD(int iid, int mid);

  public void IASTORE(int iid, int mid);

  public void LASTORE(int iid, int mid);

  public void FASTORE(int iid, int mid);

  public void DASTORE(int iid, int mid);

  public void AASTORE(int iid, int mid);

  public void BASTORE(int iid, int mid);

  public void CASTORE(int iid, int mid);

  public void SASTORE(int iid, int mid);

  public void POP(int iid, int mid);

  public void POP2(int iid, int mid);

  public void DUP(int iid, int mid);

  public void DUP_X1(int iid, int mid);

  public void DUP_X2(int iid, int mid);

  public void DUP2(int iid, int mid);

  public void DUP2_X1(int iid, int mid);

  public void DUP2_X2(int iid, int mid);

  public void SWAP(int iid, int mid);

  public void IADD(int iid, int mid);

  public void LADD(int iid, int mid);

  public void FADD(int iid, int mid);

  public void DADD(int iid, int mid);

  public void ISUB(int iid, int mid);

  public void LSUB(int iid, int mid);

  public void FSUB(int iid, int mid);

  public void DSUB(int iid, int mid);

  public void IMUL(int iid, int mid);

  public void LMUL(int iid, int mid);

  public void FMUL(int iid, int mid);

  public void DMUL(int iid, int mid);

  public void IDIV(int iid, int mid);

  public void LDIV(int iid, int mid);

  public void FDIV(int iid, int mid);

  public void DDIV(int iid, int mid);

  public void IREM(int iid, int mid);

  public void LREM(int iid, int mid);

  public void FREM(int iid, int mid);

  public void DREM(int iid, int mid);

  public void INEG(int iid, int mid);

  public void LNEG(int iid, int mid);

  public void FNEG(int iid, int mid);

  public void DNEG(int iid, int mid);

  public void ISHL(int iid, int mid);

  public void LSHL(int iid, int mid);

  public void ISHR(int iid, int mid);

  public void LSHR(int iid, int mid);

  public void IUSHR(int iid, int mid);

  public void LUSHR(int iid, int mid);

  public void IAND(int iid, int mid);

  public void LAND(int iid, int mid);

  public void IOR(int iid, int mid);

  public void LOR(int iid, int mid);

  public void IXOR(int iid, int mid);

  public void LXOR(int iid, int mid);

  public void I2L(int iid, int mid);

  public void I2F(int iid, int mid);

  public void I2D(int iid, int mid);

  public void L2I(int iid, int mid);

  public void L2F(int iid, int mid);

  public void L2D(int iid, int mid);

  public void F2I(int iid, int mid);

  public void F2L(int iid, int mid);

  public void F2D(int iid, int mid);

  public void D2I(int iid, int mid);

  public void D2L(int iid, int mid);

  public void D2F(int iid, int mid);

  public void I2B(int iid, int mid);

  public void I2C(int iid, int mid);

  public void I2S(int iid, int mid);

  public void LCMP(int iid, int mid);

  public void FCMPL(int iid, int mid);

  public void FCMPG(int iid, int mid);

  public void DCMPL(int iid, int mid);

  public void DCMPG(int iid, int mid);

  public void IRETURN(int iid, int mid);

  public void LRETURN(int iid, int mid);

  public void FRETURN(int iid, int mid);

  public void DRETURN(int iid, int mid);

  public void ARETURN(int iid, int mid);

  public void RETURN(int iid, int mid);

  public void ARRAYLENGTH(int iid, int mid);

  public void ATHROW(int iid, int mid);

  public void MONITORENTER(int iid, int mid);

  public void MONITOREXIT(int iid, int mid);


  public void LDC(String fileName, int iid, int mid, int c);

  public void LDC(String fileName, int iid, int mid, long c);

  public void LDC(String fileName, int iid, int mid, float c);

  public void LDC(String fileName, int iid, int mid, double c);

  public void LDC(String fileName, int iid, int mid, String c);

  public void LDC(String fileName, int iid, int mid, Object c);

  public void IINC(String fileName, int iid, int mid, int var, int increment);

  public void MULTIANEWARRAY(String fileName, int iid, int mid, String desc, int dims);

  public void LOOKUPSWITCH(String fileName, int iid, int mid, int dflt, int[] keys, int[] labels);

  public void TABLESWITCH(String fileName, int iid, int mid, int min, int max, int dflt, int[] labels);

  public void GOTO(String fileName, int iid, int mid, int label);

  public void JSR(String fileName, int iid, int mid, int label);

  public void INVOKEVIRTUAL(String fileName, int iid, int mid, String owner, String name, String desc);

  public void INVOKESPECIAL(String fileName, int iid, int mid, String owner, String name, String desc);

  public void INVOKESTATIC(String fileName, int iid, int mid, String owner, String name, String desc);

  public void INVOKEINTERFACE(String fileName, int iid, int mid, String owner, String name, String desc);

  public void GETSTATIC(String fileName, int iid, int mid, int cIdx, int fIdx, String desc);

  public void PUTSTATIC(String fileName, int iid, int mid, int cIdx, int fIdx, String desc);

  public void GETFIELD(String fileName, int iid, int mid, int cIdx, int fIdx, String desc);

  public void PUTFIELD(String fileName, int iid, int mid, int cIdx, int fIdx, String desc);

  public void NEW(String fileName, int iid, int mid, String type, int cIdx);

  public void ANEWARRAY(String fileName, int iid, int mid, String type);

  public void CHECKCAST(String fileName, int iid, int mid, String type);

  public void INSTANCEOF(String fileName, int iid, int mid, String type);

  public void BIPUSH(String fileName, int iid, int mid, int value);

  public void SIPUSH(String fileName, int iid, int mid, int value);

  public void NEWARRAY(String fileName, int iid, int mid);

  public void ILOAD(String fileName, int iid, int mid, int var);

  public void LLOAD(String fileName, int iid, int mid, int var);

  public void FLOAD(String fileName, int iid, int mid, int var);

  public void DLOAD(String fileName, int iid, int mid, int var);

  public void ALOAD(String fileName, int iid, int mid, int var);

  public void ISTORE(String fileName, int iid, int mid, int var);

  public void LSTORE(String fileName, int iid, int mid, int var);

  public void FSTORE(String fileName, int iid, int mid, int var);

  public void DSTORE(String fileName, int iid, int mid, int var);

  public void ASTORE(String fileName, int iid, int mid, int var);

  public void RET(String fileName, int iid, int mid, int var);

  public void NOP(String fileName, int iid, int mid);

  public void ACONST_NULL(String fileName, int iid, int mid);

  public void ICONST_M1(String fileName, int iid, int mid);

  public void ICONST_0(String fileName, int iid, int mid);

  public void ICONST_1(String fileName, int iid, int mid);

  public void ICONST_2(String fileName, int iid, int mid);

  public void ICONST_3(String fileName, int iid, int mid);

  public void ICONST_4(String fileName, int iid, int mid);

  public void ICONST_5(String fileName, int iid, int mid);

  public void LCONST_0(String fileName, int iid, int mid);

  public void LCONST_1(String fileName, int iid, int mid);

  public void FCONST_0(String fileName, int iid, int mid);

  public void FCONST_1(String fileName, int iid, int mid);

  public void FCONST_2(String fileName, int iid, int mid);

  public void DCONST_0(String fileName, int iid, int mid);

  public void DCONST_1(String fileName, int iid, int mid);

  public void IALOAD(String fileName, int iid, int mid);

  public void LALOAD(String fileName, int iid, int mid);

  public void FALOAD(String fileName, int iid, int mid);

  public void DALOAD(String fileName, int iid, int mid);

  public void AALOAD(String fileName, int iid, int mid);

  public void BALOAD(String fileName, int iid, int mid);

  public void CALOAD(String fileName, int iid, int mid);

  public void SALOAD(String fileName, int iid, int mid);

  public void IASTORE(String fileName, int iid, int mid);

  public void LASTORE(String fileName, int iid, int mid);

  public void FASTORE(String fileName, int iid, int mid);

  public void DASTORE(String fileName, int iid, int mid);

  public void AASTORE(String fileName, int iid, int mid);

  public void BASTORE(String fileName, int iid, int mid);

  public void CASTORE(String fileName, int iid, int mid);

  public void SASTORE(String fileName, int iid, int mid);

  public void POP(String fileName, int iid, int mid);

  public void POP2(String fileName, int iid, int mid);

  public void DUP(String fileName, int iid, int mid);

  public void DUP_X1(String fileName, int iid, int mid);

  public void DUP_X2(String fileName, int iid, int mid);

  public void DUP2(String fileName, int iid, int mid);

  public void DUP2_X1(String fileName, int iid, int mid);

  public void DUP2_X2(String fileName, int iid, int mid);

  public void SWAP(String fileName, int iid, int mid);

  public void IADD(String fileName, int iid, int mid);

  public void LADD(String fileName, int iid, int mid);

  public void FADD(String fileName, int iid, int mid);

  public void DADD(String fileName, int iid, int mid);

  public void ISUB(String fileName, int iid, int mid);

  public void LSUB(String fileName, int iid, int mid);

  public void FSUB(String fileName, int iid, int mid);

  public void DSUB(String fileName, int iid, int mid);

  public void IMUL(String fileName, int iid, int mid);

  public void LMUL(String fileName, int iid, int mid);

  public void FMUL(String fileName, int iid, int mid);

  public void DMUL(String fileName, int iid, int mid);

  public void IDIV(String fileName, int iid, int mid);

  public void LDIV(String fileName, int iid, int mid);

  public void FDIV(String fileName, int iid, int mid);

  public void DDIV(String fileName, int iid, int mid);

  public void IREM(String fileName, int iid, int mid);

  public void LREM(String fileName, int iid, int mid);

  public void FREM(String fileName, int iid, int mid);

  public void DREM(String fileName, int iid, int mid);

  public void INEG(String fileName, int iid, int mid);

  public void LNEG(String fileName, int iid, int mid);

  public void FNEG(String fileName, int iid, int mid);

  public void DNEG(String fileName, int iid, int mid);

  public void ISHL(String fileName, int iid, int mid);

  public void LSHL(String fileName, int iid, int mid);

  public void ISHR(String fileName, int iid, int mid);

  public void LSHR(String fileName, int iid, int mid);

  public void IUSHR(String fileName, int iid, int mid);

  public void LUSHR(String fileName, int iid, int mid);

  public void IAND(String fileName, int iid, int mid);

  public void LAND(String fileName, int iid, int mid);

  public void IOR(String fileName, int iid, int mid);

  public void LOR(String fileName, int iid, int mid);

  public void IXOR(String fileName, int iid, int mid);

  public void LXOR(String fileName, int iid, int mid);

  public void I2L(String fileName, int iid, int mid);

  public void I2F(String fileName, int iid, int mid);

  public void I2D(String fileName, int iid, int mid);

  public void L2I(String fileName, int iid, int mid);

  public void L2F(String fileName, int iid, int mid);

  public void L2D(String fileName, int iid, int mid);

  public void F2I(String fileName, int iid, int mid);

  public void F2L(String fileName, int iid, int mid);

  public void F2D(String fileName, int iid, int mid);

  public void D2I(String fileName, int iid, int mid);

  public void D2L(String fileName, int iid, int mid);

  public void D2F(String fileName, int iid, int mid);

  public void I2B(String fileName, int iid, int mid);

  public void I2C(String fileName, int iid, int mid);

  public void I2S(String fileName, int iid, int mid);

  public void LCMP(String fileName, int iid, int mid);

  public void FCMPL(String fileName, int iid, int mid);

  public void FCMPG(String fileName, int iid, int mid);

  public void DCMPL(String fileName, int iid, int mid);

  public void DCMPG(String fileName, int iid, int mid);

  public void IRETURN(String fileName, int iid, int mid);

  public void LRETURN(String fileName, int iid, int mid);

  public void FRETURN(String fileName, int iid, int mid);

  public void DRETURN(String fileName, int iid, int mid);

  public void ARETURN(String fileName, int iid, int mid);

  public void RETURN(String fileName, int iid, int mid);

  public void ARRAYLENGTH(String fileName, int iid, int mid);

  public void ATHROW(String fileName, int iid, int mid);

  public void MONITORENTER(String fileName, int iid, int mid);

  public void MONITOREXIT(String fileName, int iid, int mid);

  public void GETVALUE_double(double v);

  public void GETVALUE_long(long v);

  public void GETVALUE_Object(Object v);

  public void GETVALUE_boolean(boolean v);

  public void GETVALUE_byte(byte v);

  public void GETVALUE_char(char v);

  public void GETVALUE_float(float v);

  public void GETVALUE_int(int v);

  public void GETVALUE_short(short v);

  public void GETVALUE_void();

  public void METHOD_BEGIN(String fileName, String owner, String name, String desc);

  public void METHOD_THROW();

  public void INVOKEMETHOD_EXCEPTION();

  public void INVOKEMETHOD_END(String owner, String methodName, String desc);

  public void MAKE_SYMBOLIC();

  public void SPECIAL(int i);

  public void flush();
}
