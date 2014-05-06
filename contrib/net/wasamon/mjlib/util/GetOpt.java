package net.wasamon.mjlib.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * �����β��Ϥ�Ԥ������η�̤��ݻ����륯�饹��<br>
 * GetOpt("hvf:", "prefix:", args)�ʤɤȤ�������<br>
 * �ǽ�ΰ����ǰ�ʸ���Υ��ץ�������������Ǥϥ�󥰥��ץ��������ꡣ<br>
 * ":"�������ˤĤ��뤳�Ȥǡ����Υ��ץ����ϰ�����Ȥ뤳�Ȥ��Ǥ��롣<br>
 * ���ΰ����϶���ޤ���"="�Ǽ��̤���롣<br>
 * �ޤ������ץ�����"::"��Ĥ���ȡ����θ��ʸ���󤹤٤Ƥ򡢤��Υ��ץ����ΰ����Ȥ��ƽ������롣<br>
 * - �� -- �μ����ޤ���-�ǤϤ��ޤ�ʤ�ʸ���󤫤������Ȥ����ݻ����롣<br>
 * 
 * @version $Id: GetOpt.java,v 1.3 2004/05/24 05:24:35 miyo Exp $
 * @author Takefumi MIYOSHI
 * 
 */
public class GetOpt {

	private String[] args;

	private NamedArrayList opts;

	/** �����Ĥ��Υ��ץ������ݻ�����Υꥹ�� */
	private NamedArrayList opt_with_arg;

	/** �����Ĥ��ǤϤʤ����ץ����ե饰�Υꥹ�� */
	private NamedArrayList opt_flag;

	/** ���Ϥη���������ä��ꥹ�� */
	private ArrayList unknown;

	private NamedObject opt_with_arg_rest = new NamedObject("");

	private boolean result = true;

	/**
	 * ���󥹥ȥ饯��
	 * 
	 * @param sp
	 *            ���Ϥ�������ʸ�����ץ�����Ϣ³(-v�Ȥ�)
	 * @param lps
	 *            ���Ϥ�������󥰥��ץ�����commma separate����
	 * @param ptn
	 *            ���Ϥ��٤�ʸ���������
	 */
	public GetOpt(String sp, String lps, String ptn[]) {
		this(sp, lps, ptn, 0);
	}

	/**
	 * ���󥹥ȥ饯��
	 * 
	 * @param sp
	 *            ���Ϥ�������ʸ�����ץ�����Ϣ³(-v�Ȥ�)
	 * @param lps
	 *            ���Ϥ�������󥰥��ץ�����commma separate����
	 * @param ptn
	 *            ���Ϥ��٤�ʸ���������
	 * @param offset
	 *            ���Ϥ��٤�ʸ���Υ��ե��å�
	 */
	public GetOpt(String sp, String lps, String ptn[], int offset) {
		args = new String[0];
		opt_with_arg = new NamedArrayList();
		opt_flag = new NamedArrayList();
		opts = new NamedArrayList();
		unknown = new ArrayList();

		StringTokenizer st = new StringTokenizer(lps, ",");
		String lp[] = new String[st.countTokens()];
		for (int i = 0; i < lp.length; i++) {
			lp[i] = st.nextToken();
		}

		makeOptList(sp, lp);

		analyze(ptn, offset);

	}

	/**
	 * �ǥХå��ѥ��󥹥ȥ饯��
	 * 
	 */
	public GetOpt(String sp, String lps, String ptn[], boolean flag) {
		this(sp, lps, ptn);

		String[] o = getAllOpt();
		String[] a = getArgs();

		for (int i = 0; i < o.length; i++) {
			System.out.println("Option " + o[i]);
			if (flag(o[i])) {
				try {
					System.out.println(" Value=" + getValue(o[i]));
				} catch (GetOptException e) {
					System.out.println(" Value=");
				}
			}
		}
		if (a != null) {
			for (int i = 0; i < a.length; i++)
				System.out.println("Argument " + a[i]);
		}
	}

	/**
	 * �������٤Ƥ��Ф����ꤷ���ѥ����󤬤��뤫�ɤ���Ƚ�ꤹ��
	 * 
	 * @param ptn
	 *            ����������
	 * @param offset
	 *            ���Ϥ�������Υ��ե��å�
	 * 
	 * @TODO ��äȤ������르�ꥺ���
	 */
	private void analyze(String[] ptn, int offset) {
		int i = offset;
		for (i = offset; i < ptn.length; i++) {
			if (ptn[i].charAt(0) != '-') {
				break;
			}
			if ((ptn[i].equals("-") == true) || (ptn[i].equals("--") == true)) {
				break;
			}

			if (opt_with_arg_rest.equals(ptn[i].substring(1))) {
				String flag = ptn[i].substring(1);
				String rest = "";
				i += 1;
				while (true) {
					rest += ptn[i];
					if (i == ptn.length - 1) {
						break;
					} else {
						rest += " ";
						i += 1;
					}
				}
				opts.add(new AssocPair(flag, rest));
			}

			if (ptn[i].charAt(0) == '-') {
				if (ptn[i].charAt(1) == '-') {
					i += analy_longopt(ptn[i].substring(2), ptn, i);
				} else {
					i += analy_shortopt(ptn[i].substring(1), ptn, i);
				}
			}
		}

		args = setArgs(ptn, i);
	}

	/**
	 * Ϳ����줿���硼�ȥ��ץ����ȥ�󥰥��ץ���󤫤� �������ϤΤ���Υꥹ�Ȥ���������
	 */
	private boolean makeOptList(String sp, String[] lp) {
		int i = 0;
		while (i < sp.length()) {
			if (sp.length() > (i + 1) && sp.charAt(i + 1) == ':') { // �⤷ʸ���θ��':'��³���Ƥ�����������ȼ��
				if (sp.length() > (i + 2) && sp.charAt(i + 2) == ':') { // �⤦���³���Ƥ�����饹��
					opt_with_arg_rest = new NamedObject(sp.substring(i, i + 1));
					i += 3;
				} else {
					opt_with_arg.add(new NamedObject(sp.substring(i, i + 1)));
					i += 2;
				}
			} else {
				opt_flag.add(new NamedObject(sp.substring(i, i + 1)));
				i += 1;
			}
		}
		i = 0;
		while (i < lp.length) {
			if (lp[i].charAt(lp[i].length() - 1) == ':') { // �ǽ���ʸ����':'�ʤ������ȼ��
				opt_with_arg.add(new NamedObject(lp[i].substring(0,
						lp[i].length() - 1)));
			} else {
				opt_flag.add(new NamedObject(lp[i]));
			}
			i += 1;
		}
		return true;
	}

	/**
	 * �ѥ�����˳�������ե饰���ץ���󤬤뤫�ɤ���
	 * 
	 * @param ptn
	 *            �ѥ�����ʸ����
	 * @return �������ץ���󤬤��뤫�ɤ���
	 */
	private int analy_shortopt(String ptn, String arg[], int offset) {
		int add = 0;
		for (int i = 0; i < ptn.length(); i++) {
			String flag = ptn.substring(i, i + 1);
			if (opt_flag.has(flag)) {
				opts.add(new NamedObject(flag));
				add += 0;
			} else if (opt_with_arg.has(flag)) {
				if (arg.length > offset + 1) {
					opts.add(new AssocPair(flag, arg[offset + 1]));
					add += 1;
				} else {
					result = false;
					add += 0;
				}
			}
		}
		return add;
	}

	/**
	 * �����Ĥ����ץ����β��� hoge=fefe �ޤ��� hoge fefe �򥪥ץ���� hoge �ȡ����ΰ��� fefe �Ȳ���
	 * 
	 * @param ptn
	 *            ���᤹�ѥ�����
	 * @param arg
	 *            ���ץ������������(�������⤷��ʤ�����)
	 * @param offset
	 *            ���ߤΥѥ����������Υ��ե��å�
	 * @return �������륪�ץ���󤬤��ä����ɤ���
	 */
	private int analy_longopt(String ptn, String arg[], int offset) {
		int add = 0;
		if (opt_flag.has(ptn)) {
			opts.add(new NamedObject(ptn));
			add = 0;
		} else if (ptn.matches(".*=.*")) { // hogehoge=*�ߤ����ʷ�
			int index = ptn.indexOf("=");
			String ptn2 = ptn.substring(0, index);
			if (opt_with_arg.has(ptn2)) {
				opts.add(new AssocPair(ptn2, ptn.substring(index + 1)));
			} else {
				result = false;
			}
			add = 0;
		} else if (opt_with_arg.has(ptn)) {
			if (arg.length > offset + 1) {
				opts.add(new AssocPair(ptn, arg[offset + 1]));
				add = 1;
			} else {
				opts.add(new AssocPair(ptn, ""));
				result = true;
				add = 0;
			}
		} else {
			result = false;
			add = 0;
		}
		return add;
	}

	public boolean isSuccess() {
		return result;
	}

	/**
	 * ���ץ���󤬻��ꤵ��Ƥ������ɤ�����Ƚ�ꤹ��
	 * 
	 * @param key
	 *            �������륪�ץ����̾
	 * @return ���ꤵ��Ƥ���/���ʤ��ä�
	 */
	public boolean flag(String key) {
		return opts.has(key);
	}

	/**
	 * ���ץ����ǻ��ꤵ��Ƥ����ͤ�������롣
	 * 
	 * @param key
	 *            �������륪�ץ����̾
	 * @return ���ꤵ��Ƥ�����(ʸ����)
	 * @throws GetOptException
	 *             Ϳ����줿ʸ����Υ��ץ���󤬤ʤ����
	 */
	public String getValue(String key) throws GetOptException {
		Object obj = null;

		try {
			obj = opts.search(key);
		} catch (NoSuchException e) {
			throw new GetOptException("no such options." + key);
		}

		if (obj instanceof AssocPair) {
			return ((AssocPair) obj).getValue();
		} else {
			throw new GetOptException("this option doesn't have any value.");
		}
	}

	/**
	 * ���٤ƤΥ��ץ�������������롣
	 * 
	 * @return ���ץ���������
	 */
	private String[] getAllOpt() {
		String[] o = new String[opts.size()];
		for (int i = 0; i < opts.size(); i++) {
			o[i] = ((NamedObject) opts.get(i)).getName();
		}
		return o;
	}

	/**
	 * ���٤Ƥΰ���������ˤ����֤�
	 * 
	 * @return ����������
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * �ѥ���Τ���offset�ʹߤ�����˳�Ǽ�����֤�
	 * 
	 * @param ptn
	 *            �ѥ���
	 * @param offset
	 *            ���ե��å�
	 * @return ʸ���������
	 */
	private String[] setArgs(String[] ptn, int offset) {
		int argc = ptn.length - offset;
		String[] args = new String[argc];
		for (int i = 0; i < argc; i++) {
			args[i] = ptn[i + offset];
		}
		return args;
	}

	/**
	 * ̾�����ͤ��Ȥ�ɽ�魯���֥�������
	 */
	class AssocPair extends NamedObject {
		/** �ե饰 */
		String value;

		/**
		 * ���󥹥ȥ饯��
		 * 
		 * @param name
		 *            ̾��
		 * @param value
		 *            �ե饰
		 */
		private AssocPair(String name, String value) {
			super(name);
			this.value = value;
		}

		private String getValue() {
			return value;
		}
	}

	private void print_opt_flag() {
		for (int i = 0; i < opt_flag.size(); i++) {
			System.out.println(((NamedObject) opt_flag.get(i)).getName());
		}
	}

	private void print_opt_with_arg() {
		for (int i = 0; i < opt_with_arg.size(); i++) {
			System.out.println(((NamedObject) opt_with_arg.get(i)).getName());
		}
	}

	public static void main(String args[]) throws Exception {
		System.out.println("GetOpt test.");

		String sp = "vh";
		String lp = "version:";

		GetOpt go = new GetOpt(sp, lp, args);

		go.print_opt_flag();
		go.print_opt_with_arg();

		String[] o = go.getAllOpt();
		String[] a = go.getArgs();

		for (int i = 0; i < o.length; i++)
			System.out.println("Option " + o[i]);
		if (a != null) {
			for (int i = 0; i < a.length; i++)
				System.out.println("Argument " + a[i]);
		} else {
			System.out.println("no argument");
		}

		System.out.println(go.flag("v"));
		System.out.println(go.flag("h"));
		System.out.println(go.flag("version"));

		try {
			System.out.println(go.getValue("version"));
		} catch (GetOptException e) {
			System.out.println("not specified such option.");
		}

		System.out.println(go.isSuccess());
	}

}
