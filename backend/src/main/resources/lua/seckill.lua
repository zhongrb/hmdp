local stockKey = KEYS[1]
local orderKey = KEYS[2]
local userId = ARGV[1]

local stockValue = redis.call('get', stockKey)
if not stockValue then
  return 4
end

local stock = tonumber(stockValue)
if stock <= 0 then
  return 1
end

if redis.call('sismember', orderKey, userId) == 1 then
  return 2
end

redis.call('decrby', stockKey, 1)
redis.call('sadd', orderKey, userId)
return 0
