package com.chendezhi.baidu.tieba.capture.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CaptureDao extends BaseHibernateDAO {

	public void save(BaiduTiebaTitle title) {
		Session s = null;
		Transaction t = null;
		try {
			s = getSession();
			t = s.beginTransaction();
			s.saveOrUpdate(title);
			s.flush();
			t.commit();
		} catch (RuntimeException re) {
			t.rollback();
			throw re;
		} finally {
			closeSession();
		}
	}

	public List<BaiduTiebaTitle> listTitle(String key) {
		String hql = "from BaiduTiebaTitle where tiebaName = '" + key + "'";
		Session session = null;
		try {
			session = getSession();
			Query query = session.createQuery(hql);
			return query.list();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			closeSession();
		}
	}

	public List<BaiduTiebaTitle> listTitleNew(String key, int count) {
		String hql = "from BaiduTiebaTitle where tiebaName = '" + key
				+ "' and isNew = true";
		Session session = null;
		try {
			session = getSession();
			Query query = session.createQuery(hql);
			if (count > 0) {
				query.setFirstResult(0);
				query.setMaxResults(1000);
			}
			return query.list();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			closeSession();
		}
	}

	public void saveContent(BaiduTiebaContent content) {
		Session s = null;
		Transaction t = null;
		try {
			s = getSession();
			t = s.beginTransaction();
			s.saveOrUpdate(content);
			s.flush();
			t.commit();
		} catch (RuntimeException re) {
			t.rollback();
			throw re;
		} finally {
			closeSession();
		}
	}

	public void saveTask(BaiduTiebaContentTask task) {
		Session s = null;
		Transaction t = null;
		try {
			s = getSession();
			t = s.beginTransaction();
			s.saveOrUpdate(task);
			s.flush();
			t.commit();
		} catch (RuntimeException re) {
			t.rollback();
			throw re;
		} finally {
			closeSession();
		}
	}

	public BaiduTiebaContentTask getTask(long id) {
		String hql = "from BaiduTiebaContentTask where tiebaId = " + id;
		Session session = null;
		try {
			session = getSession();
			Query query = session.createQuery(hql);
			return (query.list().size() > 0 ? (BaiduTiebaContentTask) query
					.list().get(0) : null);
		} catch (RuntimeException e) {
			throw e;
		} finally {
			closeSession();
		}
	}

	public BaiduTiebaTitle hasInitTitle(String key) {
		List<BaiduTiebaTitle> list = listTitle(key);
		return list.size() > 0 ? list.get(0) : null;
	}

	public BaiduTiebaContentTask hasInitContent(long id) {
		return getTask(id);
	}
}
