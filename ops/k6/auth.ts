import { check } from 'k6';
import http from 'k6/http';

const KEYCLOAK_URL = 'http://keycloak.keycloak.svc.cluster.local/realms/cinema/protocol/openid-connect/token';
const CLIENT_ID = 'client-user';
const USERNAME = 'quentin';
const PASSWORD = 'test';

export function getAccessToken() {
    const authData = {
        client_id: CLIENT_ID,
        username: USERNAME,
        password: PASSWORD,
        grant_type: 'password',
    };

    const authRes = http.post(KEYCLOAK_URL, authData, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    });

    check(authRes, {
        'Keycloak token obtained': (r) => r.status === 200 && r.json('access_token') !== undefined,
    });

    if (authRes.status === 200) {
        const accessToken = authRes.json('access_token')?.toString();
        return { accessToken };
    } else {
        console.error(`Failed to obtain access token: ${authRes.body}`);
        return null;
    }
}



export default function () {
    const data = getAccessToken();

    if (!data?.accessToken) {
        console.error('No access token provided');
        return;
    }

    const headers = {
        'Authorization': `Bearer ${data.accessToken}`,
        'Content-Type': 'application/json',
    };

    const getRes = http.get('http://scr.hp.kind/api/actors', { headers: headers });
    check(getRes, {
        'GET actors status is 200': (r) => (r.status === 200 || r.status === 206),
    });

    if (getRes.status === 200 || getRes.status === 206) {
        const actors = getRes.json();
    } else {
        console.error(`Failed to retrieve actors: ${getRes.body}`);
    }
}

export const options = {
  stages: [
    { duration: '10s', target: 1 },
    { duration: '20s', target: 1 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'], // Moins de 1% d'échecs de requêtes HTTP
    http_req_duration: ['p(95)<200'], // 95% des requêtes doivent être en dessous de 200ms
  },
};