package com.chendezhi.baidu.tieba.capture.hibernate;

import org.hibernate.Session;

public class BaseHibernateDAO implements IBaseHibernateDAO {

	public Session getSession() {
		return HibernateSessionFactory.getSession();
	}

	protected void closeSession() {
		HibernateSessionFactory.closeSession();
	}

}
