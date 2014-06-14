package com.chendezhi.baidu.tieba.capture.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactory {
	static Logger logger = Logger.getLogger(HibernateSessionFactory.class);
	static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
	static SessionFactory sessionFactory;

	public HibernateSessionFactory() {
	}

	public static void initSessionFactory(String filePath) {
		Configuration configuration = new Configuration();
		configuration.configure(filePath);
		sessionFactory = configuration.buildSessionFactory();
	}

	public static Session getSession() {
		Session session = (Session) threadLocal.get();

		if (session == null || !session.isOpen()) {
			if (sessionFactory == null) {
				//
			}
			session = (sessionFactory != null) ? sessionFactory.openSession()
					: null;
			threadLocal.set(session);
		}
		return session;
	}

	public static void closeSession() {
		Session session = (Session) threadLocal.get();
		threadLocal.set(null);

		if (session != null) {
			session.close();
		}
	}
}
