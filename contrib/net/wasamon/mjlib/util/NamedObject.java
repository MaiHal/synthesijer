package net.wasamon.mjlib.util;


/**
 * ̾���Ĥ����֥������Ȥδ��쥯�饹
 * 
 * @version $Id: NamedObject.java,v 1.1.1.1 2003/12/29 11:46:16 miyo Exp $
 * @author Takefumi MIYOSHI
 * 
 */
public class NamedObject extends Object{

  private String name;

  /**
   * ̾������Ͽ�������󥿥󥹤�����
   * @param name ���Υ��֥������ȤˤĤ���̾��
   */
  public NamedObject(String name){
    this.name = name;
  }

  /**
   * ���Υ��󥹥��󥹤�̾�����֤�
   */
  public String getName(){
    return name;
  }

  public boolean equals(String n){
    return name.equals(n);
  }

}
