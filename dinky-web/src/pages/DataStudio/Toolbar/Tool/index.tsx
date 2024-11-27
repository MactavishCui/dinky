/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import { Tabs } from 'antd';
import TextComparison from '@/pages/DataStudio/Toolbar/Tool/TextComparison';
import { JsonToSql } from '@/pages/DataStudio/Toolbar/Tool/JsonToSql';
import './index.less';

export default () => {
  const items = [
    {
      key: 'jsonToSql',
      label: 'JSON转Flink-SQL',
      children: <JsonToSql />
    },
    {
      key: 'textComparison',
      label: '文本对比',
      children: <TextComparison />
    }
  ];
  return (
    <div style={{ padding: 10, height: '100%' }}>
      <Tabs items={items} size={'small'} style={{ height: '100%' }} />
    </div>
  );
};