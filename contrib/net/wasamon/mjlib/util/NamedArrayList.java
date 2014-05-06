package net.wasamon.mjlib.util;

import java.util.ArrayList;

/**
 * ̾���դ��Υ��쥤�ꥹ�ȥ��饹
 *
 * @version $Id: NamedArrayList.java,v 1.1.1.1 2003/12/29 11:46:16 miyo Exp $
 * @author Takefumi MIYOSHI
 *
 */
public class NamedArrayList extends ArrayList{
  
  /**
   * ���󥹥ȥ饯��
   */
  public NamedArrayList(){
    super();
  }

  /**
   * ̾���򥭡��ˤ���ArrayList��ꥪ�֥������Ȥ򸡺������Ĥ�����Τ��֤���
   * @param name �����Ȥʤ�̾��
   * @return ����̾���Υ��֥�������
   * @throws NoSuchException �ߤĤ���ʤ��ä�����ȯ�������㳰
   */
  public NamedObject search(String name) throws NoSuchException{
    NamedObject obj = null;
    for(int i = 0; i < super.size(); i++){
      obj = (NamedObject)(super.get(i));
      if(name.equals(obj.getName()) == true){
	return obj;
      }
    }
    throw new NoSuchException(name + " is not found.");
  }

  public boolean has(String name){
    try{
      search(name);
    }catch(NoSuchException e){
      return false;
    }
    return true;
  }

}

