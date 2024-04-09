package usw.suwiki.comon.test.support;

import usw.suwiki.comon.test.db.Table;

import java.util.Set;

public interface DatabaseCleaner {
  void clean(Set<Table> tables);
}
