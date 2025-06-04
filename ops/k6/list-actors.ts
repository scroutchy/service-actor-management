import { check } from 'k6';
import http from 'k6/http';
import { getAccessToken } from './helper/token.ts';
import { paths } from './helper/url.ts';


export default function () {
    const authData = getAccessToken();

    if (!authData?.accessToken) {
        console.error('No access token provided');
        return;
    }

    const headers = {
        'Authorization': `Bearer ${authData.accessToken}`,
        'Content-Type': 'application/json',
    };

    const getRes = http.get(paths.actors, { headers: headers });
    check(getRes, {
        'GET actors status is 200': (r) => (r.status === 200 || r.status === 206),
    });

    if (getRes.status !== 200 && getRes.status !== 206) {
        console.error(`Failed to list actors: ${getRes.body}`);
    }
}

/*export const options = {
  stages: [
    { duration: '10s', target: 1 },
    { duration: '20s', target: 1 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'], // Moins de 1% d'échecs de requêtes HTTP
    http_req_duration: ['p(95)<200'], // 95% des requêtes doivent être en dessous de 200ms
  },
};*/