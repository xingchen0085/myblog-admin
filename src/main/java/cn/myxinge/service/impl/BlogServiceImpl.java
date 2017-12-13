package cn.myxinge.service.impl;

import cn.myxinge.dao.BlogDao;
import cn.myxinge.dao.ResourceDao;
import cn.myxinge.entity.Archives;
import cn.myxinge.entity.Blog;
import cn.myxinge.service.BlogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Created by chenxinghua on 2017/11/9.
 */
@Service
@Transactional
public class BlogServiceImpl extends BaseServiceImpl<Blog> implements BlogService {
    private static final Logger LOG = LoggerFactory.getLogger(BlogServiceImpl.class);
    private BlogDao blogDao;
    @Autowired
    private ResourceDao resourceDao;

    @Override
//    @Cacheable
    public Blog getBlogByUrl(String url) {
        Blog b = new Blog();
        b.setUrl(url);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("url", ExampleMatcher.GenericPropertyMatchers.caseSensitive());

        List<Blog> list = blogDao.findAll(Example.of(b, matcher));

        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public void update(Blog blog) {
        blog.setUpdatetime(new Date());
        super.update(blog);
    }

    @Override
    public Map<String, Blog> findPreAndNext(Blog blog) {
        //HashMap即是value为null也不会抛出异常
        Map<String, Blog> map = new HashMap<String, Blog>();
        map.put("preBlog", blogDao.preBlog(blog.getCreatetime()));
        map.put("nextBlog", blogDao.nextBlog(blog.getCreatetime()));
        return map;
    }

    @Override
    public List<Archives> listByArchives() {
        List<Blog> all = listAll();//所有数据，上线的

        if (null == all || all.size() < 1) {
            return null;
        }

        List<Archives> archivesList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(all.get(all.size() - 1).getCreatetime());
        int earlyYear = calendar.get(Calendar.YEAR);
        calendar.setTime(new Date());
        int curYear = calendar.get(Calendar.YEAR);

        Archives archive = null;
        List<Map<String, List<Blog>>> datas = null;
        Map<String, List<Blog>> monthData = null;
        List<Blog> list = null;
        for (; earlyYear <= curYear; earlyYear++) {
            archive = new Archives();
            datas = new ArrayList<>();
            archive.setYear(earlyYear + "");

            for (int i = 12; i >= 1; i--) {
                list = new ArrayList<>();
                monthData = new HashMap<>();
                for (Blog b : all) {
                    calendar.setTime(b.getCreatetime());
                    if (calendar.get(Calendar.MONTH) + 1 == i) {
                        list.add(b);
                    }
                }

                if (list.size() > 0) {
                    monthData.put(i + "", list);
                    datas.add(monthData);
                }
            }
            archive.setDatas(datas);
            archivesList.add(archive);
        }
        return archivesList;
    }


    //返回所有已上线的数据/无分页
    public List<Blog> listAll() {
        Blog b = new Blog();
        b.setState(Blog.STATE_ONLINE);

        Sort sort = new Sort(Sort.Direction.DESC, "createtime");

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("state", ExampleMatcher.GenericPropertyMatchers.caseSensitive());

        List<Blog> list = blogDao.findAll(Example.of(b, matcher), sort);
        return list;
    }

    @Autowired
    public void setBlogDao(BlogDao blogDao) {
        this.blogDao = blogDao;
        super.setJpaRepository(blogDao);
    }
}












