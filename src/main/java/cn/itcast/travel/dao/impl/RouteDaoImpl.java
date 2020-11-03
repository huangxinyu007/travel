package cn.itcast.travel.dao.impl;

import cn.itcast.travel.dao.RouteDao;
import cn.itcast.travel.domain.Route;
import cn.itcast.travel.util.JDBCUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouteDaoImpl implements RouteDao {
    private JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());

    @Override
    public int findTotalCount(int cid,String rname) {
        //String sql = "select count(*) from tab_route where cid = ?";
        //1.定义sql模板
        String sql = "select count(*) from tab_route where 1=1 ";
        StringBuilder sb = new StringBuilder(sql);

        List params = new ArrayList();//条件们
        //2.判断参数是否有值
        if(cid != 0){
            sb.append( " and cid = ? ");

            params.add(cid);//添加？对应的值
        }

        if(rname != null && rname.length() > 0 && !"null".equalsIgnoreCase(rname)){
            sb.append(" and rname like ? ");

            params.add("%"+rname+"%");
        }

        sql = sb.toString();


        return template.queryForObject(sql,Integer.class,params.toArray());
    }

    @Override
    public List<Route> findByPage(int cid, int start, int pageSize,String rname) {
        //String sql = "select * from tab_route where cid = ? and rname like ?  limit ? , ?";
        String sql = " select * from tab_route where 1 = 1 ";
        //1.定义sql模板
        StringBuilder sb = new StringBuilder(sql);

        List params = new ArrayList();//条件们
        //2.判断参数是否有值
        if(cid != 0){
            sb.append( " and cid = ? ");

            params.add(cid);//添加？对应的值
        }

        if(rname != null && rname.length() > 0 && !"null".equalsIgnoreCase(rname)){
            sb.append(" and rname like ? ");

            params.add("%"+rname+"%");
        }
        sb.append(" limit ? , ? ");//分页条件

        sql = sb.toString();

        params.add(start);
        params.add(pageSize);


        return template.query(sql,new BeanPropertyRowMapper<Route>(Route.class),params.toArray());
    }

    @Override
    public List<Route> findByPageFavourite() {
        //String sql = "select * from tab_route where cid = ? and rname like ?  limit ? , ?";
        String sql = " select * from tab_route where 1 = 1 ";
        //1.定义sql模板
        StringBuilder sb = new StringBuilder(sql);
        sb.append(" order by count desc ");//分页条件
        sql = sb.toString();
        List params = new ArrayList();//条件们
        return template.query(sql,new BeanPropertyRowMapper<Route>(Route.class),params.toArray());
    }

    @Override
    public Route findOne(int rid) {
        String sql = "select * from tab_route where rid = ?";
        return template.queryForObject(sql,new BeanPropertyRowMapper<Route>(Route.class),rid);
    }

    @Override
    public List<Route> queryFavourite() {
        String sql = " SELECT rid, COUNT(rid) AS num FROM tab_favorite GROUP BY rid ORDER BY num DESC ";
        List<Integer> ridList = new ArrayList<>(4);
        template.query(sql, new RowMapper<List<Integer>>() {
            @Override
            public List<Integer> mapRow(ResultSet rs, int i) throws SQLException {
                int rid = rs.getInt("rid");
                if (ridList.size() < 4) {
                    ridList.add(rid);
                }
                return null;
            }
        });
        List params = new ArrayList();
        String numStr = "";
        for (int i = 0; i < ridList.size(); i++) {
            if (i == ridList.size() - 1) {
                numStr += " ? ";
            } else {
                numStr += " ?, ";
            }
            params.add(ridList.get(i));
        }
        String sql1 = " select * from tab_route where rid IN (" +numStr+ ") ";
        return template.query(sql1, new BeanPropertyRowMapper<Route>(Route.class), params.toArray());
    }

    @Override
    public List<Route> queryRandom() {
        String sql = " select * from tab_route where rid in (";
        StringBuilder sb = new StringBuilder(sql);
        List params = new ArrayList();//条件们
        int totalCount = findTotalCount(0, "");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                sb.append("?)");
            } else {
                sb.append("?, ");
            }
            params.add(random.nextInt(totalCount));
        }
        return template.query(sb.toString(), new BeanPropertyRowMapper<Route>(Route.class), params.toArray());
    }

    @Override
    public void updateRouteByRid(int count, int rid) {
        String sql = " update tab_route set count = ? where rid = ? ";
        List params = new ArrayList();
        params.add(count);
        params.add(rid);
        template.update(sql, params.toArray());
    }
}
